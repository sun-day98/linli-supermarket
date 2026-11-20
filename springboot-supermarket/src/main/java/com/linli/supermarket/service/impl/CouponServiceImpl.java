package com.linli.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linli.supermarket.common.BusinessException;
import com.linli.supermarket.common.PageResult;
import com.linli.supermarket.common.ResultCode;
import com.linli.supermarket.dto.CouponDTO;
import com.linli.supermarket.entity.Coupon;
import com.linli.supermarket.entity.Orders;
import com.linli.supermarket.entity.UserCoupon;
import com.linli.supermarket.mapper.CouponMapper;
import com.linli.supermarket.mapper.OrdersMapper;
import com.linli.supermarket.mapper.UserCouponMapper;
import com.linli.supermarket.service.CouponService;
import com.linli.supermarket.vo.CouponVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 优惠券服务实现：校验时效/归属/门槛 + 状态 CAS（0未用 3锁定 1已用）。 */
@Service
public class CouponServiceImpl implements CouponService {

    private static final Logger log = LoggerFactory.getLogger(CouponServiceImpl.class);
    private static final BigDecimal BIG_AMOUNT = new BigDecimal("999999999");
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final int STATUS_UNUSED = 0;
    private static final int STATUS_USED = 1;
    private static final int STATUS_LOCKED = 3;

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CouponVO> available(Long userId, BigDecimal amount) {
        BigDecimal base = amount == null ? BigDecimal.ZERO : amount;
        List<UserCoupon> held = userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, STATUS_UNUSED)
                .orderByAsc(UserCoupon::getGetTime));
        Map<Long, Coupon> couponMap = loadCoupons(held);
        List<CouponVO> result = new ArrayList<>();
        for (UserCoupon uc : held) {
            Coupon coupon = couponMap.get(uc.getCouponId());
            if (coupon == null || checkUsable(coupon, uc, base) != null) {
                continue;
            }
            result.add(toVO(coupon, uc, true, null));
        }
        return result;
    }

    /** 批量加载券定义，避免逐条 selectById 造成 N+1。 */
    private Map<Long, Coupon> loadCoupons(List<UserCoupon> held) {
        if (held == null || held.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ids = held.stream().map(UserCoupon::getCouponId)
                .filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return couponMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Coupon::getId, Function.identity(), (a, b) -> a));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponVO> my(Long userId) {
        List<UserCoupon> held = userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .orderByDesc(UserCoupon::getGetTime));
        Map<Long, Coupon> couponMap = loadCoupons(held);
        List<CouponVO> result = new ArrayList<>();
        for (UserCoupon uc : held) {
            Coupon coupon = couponMap.get(uc.getCouponId());
            if (coupon == null) {
                continue;
            }
            String reason = checkUsable(coupon, uc, BIG_AMOUNT);
            result.add(toVO(coupon, uc, reason == null, reason));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claim(Long userId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getStatus() == null || coupon.getStatus() != 1) {
            throw new BusinessException(ResultCode.COUPON_INVALID, "优惠券不存在或已停用");
        }
        Long heldCount = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId));
        if (heldCount != null && heldCount > 0) {
            throw new BusinessException(ResultCode.COUPON_INVALID, "您已领取过该优惠券");
        }
        if (coupon.getScope() != null && coupon.getScope() == 2) {
            Long orderCount = ordersMapper.selectCount(new LambdaQueryWrapper<Orders>()
                    .eq(Orders::getUserId, userId));
            if (orderCount != null && orderCount > 0) {
                throw new BusinessException(ResultCode.COUPON_INVALID, "新人专享券仅限新用户领取");
            }
        }
        if (couponMapper.increaseIssued(couponId) == 0) {
            throw new BusinessException(ResultCode.COUPON_INVALID, "优惠券已被领完");
        }
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus(STATUS_UNUSED);
        uc.setGetTime(LocalDateTime.now());
        uc.setCreatedAt(LocalDateTime.now());
        uc.setUpdatedAt(LocalDateTime.now());
        userCouponMapper.insert(uc);
    }

    @Override
    public PageResult<CouponVO> adminPage(Integer current, Integer size) {
        IPage<Coupon> page = couponMapper.selectPage(new Page<>(current == null ? 1 : current, size == null ? 10 : size),
                new LambdaQueryWrapper<Coupon>().orderByDesc(Coupon::getId));
        return PageResult.of(page, coupon -> toVO(coupon, null, null, null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAdmin(CouponDTO dto) {
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(dto, coupon);
        coupon.setIssued(0);
        if (coupon.getStatus() == null) {
            coupon.setStatus(1);
        }
        if (coupon.getValidType() == null) {
            coupon.setValidType(1);
        }
        if (coupon.getScope() == null) {
            coupon.setScope(1);
        }
        if (coupon.getNewUserAuto() == null) {
            coupon.setNewUserAuto(0);
        }
        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setUpdatedAt(LocalDateTime.now());
        couponMapper.insert(coupon);
        return coupon.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAdmin(Long id, CouponDTO dto) {
        if (couponMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "优惠券不存在");
        }
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(dto, coupon);
        coupon.setId(id);
        coupon.setUpdatedAt(LocalDateTime.now());
        couponMapper.updateById(coupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAdmin(Long id) {
        couponMapper.deleteById(id);
        userCouponMapper.delete(new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getCouponId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean validateAndLock(Long userId, Long userCouponId, BigDecimal amount) {
        Coupon coupon = validateForPreview(userId, userCouponId, amount);
        if (coupon == null) {
            return false;
        }
        if (userCouponMapper.lock(userCouponId) == 0) {
            throw new BusinessException(ResultCode.COUPON_INVALID, "优惠券已被占用，请刷新后重试");
        }
        return true;
    }

    @Override
    public Coupon validateForPreview(Long userId, Long userCouponId, BigDecimal amount) {
        if (userCouponId == null) {
            return null;
        }
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc == null || !userId.equals(uc.getUserId())) {
            throw new BusinessException(ResultCode.COUPON_INVALID, "优惠券不属于当前用户");
        }
        Coupon coupon = couponMapper.selectById(uc.getCouponId());
        if (coupon == null) {
            throw new BusinessException(ResultCode.COUPON_INVALID, "优惠券不存在");
        }
        String reason = checkUsable(coupon, uc, amount == null ? BigDecimal.ZERO : amount);
        if (reason != null) {
            throw new BusinessException(ResultCode.COUPON_INVALID, reason);
        }
        return coupon;
    }

    @Override
    public void consume(Long userCouponId, Long orderId) {
        if (userCouponId == null) {
            return;
        }
        int affected = userCouponMapper.consume(userCouponId, orderId);
        if (affected == 0) {
            // 核销失败：券可能已被并发消费或状态异常。抛异常触发支付事务回滚，
            // 避免"支付成功但券仍滞留锁定态(3)"的不一致。
            log.warn("优惠券核销失败：userCouponId={}, orderId={}，将回滚支付事务", userCouponId, orderId);
            throw new BusinessException(ResultCode.COUPON_INVALID, "优惠券核销失败，请重试");
        }
    }

    @Override
    public void release(Long userCouponId) {
        if (userCouponId == null) {
            return;
        }
        userCouponMapper.release(userCouponId);
    }

    @Override
    public Coupon getCoupon(Long couponId) {
        return couponId == null ? null : couponMapper.selectById(couponId);
    }

    @Override
    public UserCoupon getUserCoupon(Long userCouponId) {
        return userCouponId == null ? null : userCouponMapper.selectById(userCouponId);
    }

    @Override
    public BigDecimal calcDiscount(Coupon coupon, BigDecimal goodsAmount) {
        if (coupon == null || goodsAmount == null || goodsAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return ZERO;
        }
        BigDecimal value = coupon.getDiscount() == null ? BigDecimal.ZERO : coupon.getDiscount();
        BigDecimal discount;
        if (coupon.getType() != null && coupon.getType() == 2) {
            // 折扣券：discount 为抵扣比例，如 0.20 表示减免商品金额的 20%
            discount = goodsAmount.multiply(value).setScale(2, RoundingMode.HALF_UP);
        } else {
            // 满减券 / 无门槛券：discount 为固定抵扣金额
            discount = value.setScale(2, RoundingMode.HALF_UP);
        }
        if (discount.compareTo(goodsAmount) > 0) {
            discount = goodsAmount.setScale(2, RoundingMode.HALF_UP);
        }
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            discount = ZERO;
        }
        return discount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantNewUserCoupons(Long userId) {
        List<Coupon> newUserCoupons = couponMapper.selectList(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getNewUserAuto, 1)
                .eq(Coupon::getStatus, 1));
        for (Coupon coupon : newUserCoupons) {
            Long held = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                    .eq(UserCoupon::getUserId, userId)
                    .eq(UserCoupon::getCouponId, coupon.getId()));
            if (held != null && held > 0) {
                continue;
            }
            if (coupon.getTotal() != null && coupon.getIssued() != null
                    && coupon.getIssued() >= coupon.getTotal()) {
                continue;
            }
            couponMapper.increaseIssued(coupon.getId());
            UserCoupon uc = new UserCoupon();
            uc.setUserId(userId);
            uc.setCouponId(coupon.getId());
            uc.setStatus(STATUS_UNUSED);
            uc.setGetTime(LocalDateTime.now());
            uc.setCreatedAt(LocalDateTime.now());
            uc.setUpdatedAt(LocalDateTime.now());
            userCouponMapper.insert(uc);
        }
    }

    /** 返回不可用原因；返回 null 表示可用。 */
    private String checkUsable(Coupon coupon, UserCoupon uc, BigDecimal amount) {
        if (coupon.getStatus() == null || coupon.getStatus() != 1) {
            return "优惠券已停用";
        }
        if (uc.getStatus() == null || uc.getStatus() != STATUS_UNUSED) {
            return "优惠券已使用或已锁定";
        }
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getValidType() != null && coupon.getValidType() == 1) {
            if (coupon.getStartTime() != null && now.isBefore(coupon.getStartTime())) {
                return "优惠券尚未生效";
            }
            if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
                return "优惠券已过期";
            }
        } else {
            LocalDateTime base = uc.getGetTime() == null ? now : uc.getGetTime();
            int days = coupon.getValidDays() == null ? 30 : coupon.getValidDays();
            if (base.plusDays(days).isBefore(now)) {
                return "优惠券已过期";
            }
        }
        BigDecimal threshold = coupon.getThreshold() == null ? BigDecimal.ZERO : coupon.getThreshold();
        if (amount == null || amount.compareTo(threshold) < 0) {
            return "订单金额未达到使用门槛";
        }
        return null;
    }

    private CouponVO toVO(Coupon coupon, UserCoupon uc, Boolean usable, String reason) {
        CouponVO vo = new CouponVO();
        BeanUtils.copyProperties(coupon, vo);
        if (coupon.getType() == 1) {
            vo.setTypeName("满减券");
        } else if (coupon.getType() == 2) {
            vo.setTypeName("折扣券");
        } else if (coupon.getType() == 3) {
            vo.setTypeName("无门槛券");
        } else {
            vo.setTypeName("优惠券");
        }
        if (uc != null) {
            vo.setUserCouponId(uc.getId());
            vo.setUserCouponStatus(uc.getStatus());
            vo.setGetTime(uc.getGetTime());
            vo.setUseTime(uc.getUseTime());
        }
        vo.setUsable(Boolean.TRUE.equals(usable));
        vo.setUnusableReason(reason);
        return vo;
    }
}
