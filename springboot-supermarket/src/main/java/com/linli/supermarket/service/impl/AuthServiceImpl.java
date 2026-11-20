package com.linli.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linli.supermarket.common.BusinessException;
import com.linli.supermarket.common.ResultCode;
import com.linli.supermarket.dto.AdminLoginDTO;
import com.linli.supermarket.dto.LoginDTO;
import com.linli.supermarket.entity.Admin;
import com.linli.supermarket.entity.Address;
import com.linli.supermarket.entity.User;
import com.linli.supermarket.mapper.AdminMapper;
import com.linli.supermarket.mapper.AddressMapper;
import com.linli.supermarket.mapper.UserMapper;
import com.linli.supermarket.service.AuthService;
import com.linli.supermarket.service.CouponService;
import com.linli.supermarket.util.PasswordUtil;
import com.linli.supermarket.util.TokenUtil;
import com.linli.supermarket.vo.UserVO;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 鉴权与会员服务实现。 */
@Service
public class AuthServiceImpl implements AuthService {

    @org.springframework.beans.factory.annotation.Value("${app.auth.demo-verify-code:123456}")
    private String demoVerifyCode;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private CouponService couponService;

    @Autowired
    private TokenUtil tokenUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO login(LoginDTO dto) {
        if (demoVerifyCode == null || demoVerifyCode.isBlank()) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "验证码服务未配置");
        }
        if (!demoVerifyCode.equals(dto.getCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR,
                    "验证码错误，演示环境请输入 " + demoVerifyCode);
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, dto.getPhone()).last("LIMIT 1"));
        if (user == null) {
            user = autoRegister(dto.getPhone());
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "账号已被禁用");
        }
        return toVO(user, tokenUtil.createToken(user.getId(), "USER"));
    }

    @Override
    public String adminLogin(AdminLoginDTO dto) {
        Admin admin = getAdminByUsername(dto.getUsername());
        if (admin == null || admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "账号或密码错误");
        }
        if (!PasswordUtil.matches(dto.getPassword(), admin.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "账号或密码错误");
        }
        return tokenUtil.createToken(admin.getId(), "ADMIN");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User autoRegister(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickname("邻里用户" + phone.substring(phone.length() - 4));
        user.setGender(0);
        user.setPoints(0);
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        Address address = new Address();
        address.setUserId(user.getId());
        address.setReceiver(user.getNickname());
        address.setPhone(phone);
        address.setProvince("北京市");
        address.setCity("北京市");
        address.setDistrict("朝阳区");
        address.setDetail("请填写详细门牌号");
        address.setIsDefault(1);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        addressMapper.insert(address);

        couponService.grantNewUserCoupons(user.getId());
        return user;
    }

    @Override
    public UserVO me(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户不存在");
        }
        return toVO(user, null);
    }

    @Override
    public Admin getAdminByUsername(String username) {
        List<Admin> admins = adminMapper.selectList(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, username).last("LIMIT 1"));
        return admins.isEmpty() ? null : admins.get(0);
    }

    private UserVO toVO(User user, String token) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setToken(token);
        return vo;
    }
}
