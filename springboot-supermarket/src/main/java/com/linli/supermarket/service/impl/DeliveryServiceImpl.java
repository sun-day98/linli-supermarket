package com.linli.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linli.supermarket.common.BusinessException;
import com.linli.supermarket.common.ResultCode;
import com.linli.supermarket.dto.DeliveryAreaDTO;
import com.linli.supermarket.entity.DeliveryArea;
import com.linli.supermarket.entity.Deliveryman;
import com.linli.supermarket.mapper.DeliveryAreaMapper;
import com.linli.supermarket.mapper.DeliverymanMapper;
import com.linli.supermarket.service.DeliveryService;
import com.linli.supermarket.vo.DeliveryAreaVO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 配送区域与配送员服务实现。 */
@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    private DeliveryAreaMapper deliveryAreaMapper;

    @Autowired
    private DeliverymanMapper deliverymanMapper;

    @Override
    public List<DeliveryAreaVO> treeEnabled() {
        List<DeliveryArea> cities = deliveryAreaMapper.selectList(new LambdaQueryWrapper<DeliveryArea>()
                .eq(DeliveryArea::getType, 1)
                .eq(DeliveryArea::getEnabled, 1)
                .orderByAsc(DeliveryArea::getSort)
                .orderByAsc(DeliveryArea::getId));
        List<DeliveryAreaVO> result = new ArrayList<>();
        for (DeliveryArea city : cities) {
            DeliveryAreaVO vo = toVO(city);
            List<DeliveryArea> districts = deliveryAreaMapper.selectList(new LambdaQueryWrapper<DeliveryArea>()
                    .eq(DeliveryArea::getParentId, city.getId())
                    .eq(DeliveryArea::getType, 2)
                    .eq(DeliveryArea::getEnabled, 1)
                    .orderByAsc(DeliveryArea::getSort));
            if (districts.isEmpty()) {
                continue;
            }
            vo.setChildren(districts.stream().map(this::toVO).collect(Collectors.toList()));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<DeliveryAreaVO> treeAll() {
        List<DeliveryArea> cities = deliveryAreaMapper.selectList(new LambdaQueryWrapper<DeliveryArea>()
                .eq(DeliveryArea::getType, 1)
                .orderByAsc(DeliveryArea::getSort)
                .orderByAsc(DeliveryArea::getId));
        List<DeliveryAreaVO> result = new ArrayList<>();
        for (DeliveryArea city : cities) {
            DeliveryAreaVO vo = toVO(city);
            List<DeliveryArea> districts = deliveryAreaMapper.selectList(new LambdaQueryWrapper<DeliveryArea>()
                    .eq(DeliveryArea::getParentId, city.getId())
                    .eq(DeliveryArea::getType, 2)
                    .orderByAsc(DeliveryArea::getSort));
            vo.setChildren(districts.stream().map(this::toVO).collect(Collectors.toList()));
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createArea(DeliveryAreaDTO dto) {
        DeliveryArea area = new DeliveryArea();
        BeanUtils.copyProperties(dto, area);
        if (area.getParentId() == null) {
            area.setParentId(0L);
        }
        if (area.getEnabled() == null) {
            area.setEnabled(1);
        }
        if (area.getHot() == null) {
            area.setHot(0);
        }
        if (area.getSort() == null) {
            area.setSort(0);
        }
        area.setCreatedAt(LocalDateTime.now());
        area.setUpdatedAt(LocalDateTime.now());
        deliveryAreaMapper.insert(area);
        return area.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArea(Long id, DeliveryAreaDTO dto) {
        if (deliveryAreaMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "配送区域不存在");
        }
        DeliveryArea area = new DeliveryArea();
        BeanUtils.copyProperties(dto, area);
        area.setId(id);
        area.setUpdatedAt(LocalDateTime.now());
        deliveryAreaMapper.updateById(area);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArea(Long id) {
        deliveryAreaMapper.deleteById(id);
        deliveryAreaMapper.delete(new LambdaQueryWrapper<DeliveryArea>().eq(DeliveryArea::getParentId, id));
    }

    @Override
    public boolean deliverable(String district) {
        if (district == null || district.isBlank()) {
            return false;
        }
        List<DeliveryArea> areas = deliveryAreaMapper.selectList(new LambdaQueryWrapper<DeliveryArea>()
                .eq(DeliveryArea::getName, district.trim())
                .eq(DeliveryArea::getType, 2)
                .eq(DeliveryArea::getEnabled, 1));
        for (DeliveryArea area : areas) {
            DeliveryArea city = deliveryAreaMapper.selectById(area.getParentId());
            if (city != null && city.getEnabled() != null && city.getEnabled() == 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Deliveryman> listDeliverymen() {
        return deliverymanMapper.selectList(new LambdaQueryWrapper<Deliveryman>()
                .orderByDesc(Deliveryman::getStatus)
                .orderByAsc(Deliveryman::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDeliveryman(Deliveryman deliveryman) {
        deliveryman.setId(null);
        if (deliveryman.getStatus() == null) {
            deliveryman.setStatus(1);
        }
        deliveryman.setCreatedAt(LocalDateTime.now());
        deliveryman.setUpdatedAt(LocalDateTime.now());
        deliverymanMapper.insert(deliveryman);
        return deliveryman.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeliveryman(Long id, Deliveryman deliveryman) {
        deliveryman.setId(id);
        deliveryman.setUpdatedAt(LocalDateTime.now());
        deliverymanMapper.updateById(deliveryman);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeliveryman(Long id) {
        deliverymanMapper.deleteById(id);
    }

    private DeliveryAreaVO toVO(DeliveryArea area) {
        DeliveryAreaVO vo = new DeliveryAreaVO();
        BeanUtils.copyProperties(area, vo);
        return vo;
    }
}
