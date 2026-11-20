package com.linli.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linli.supermarket.common.BusinessException;
import com.linli.supermarket.common.ResultCode;
import com.linli.supermarket.dto.AddressDTO;
import com.linli.supermarket.entity.Address;
import com.linli.supermarket.mapper.AddressMapper;
import com.linli.supermarket.service.AddressService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 收货地址服务实现。 */
@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public List<Address> list(Long userId) {
        return addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreatedAt));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, AddressDTO dto) {
        long count = addressMapper.selectCount(new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId));
        boolean useAsDefault = dto.getIsDefault() != null && dto.getIsDefault() == 1 || count == 0;
        if (useAsDefault) {
            clearDefaults(userId);
        }
        Address address = new Address();
        BeanUtils.copyProperties(dto, address);
        address.setUserId(userId);
        address.setIsDefault(useAsDefault ? 1 : 0);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        addressMapper.insert(address);
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, Long id, AddressDTO dto) {
        Address address = getOwned(userId, id);
        BeanUtils.copyProperties(dto, address);
        address.setId(id);
        address.setUserId(userId);
        address.setUpdatedAt(LocalDateTime.now());
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearDefaults(userId);
            address.setIsDefault(1);
        }
        addressMapper.updateById(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {
        getOwned(userId, id);
        addressMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long id) {
        getOwned(userId, id);
        clearDefaults(userId);
        Address address = new Address();
        address.setId(id);
        address.setIsDefault(1);
        address.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(address);
    }

    @Override
    public Address getOwned(Long userId, Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "地址不能为空");
        }
        Address address = addressMapper.selectById(id);
        if (address == null || !userId.equals(address.getUserId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "收货地址不存在");
        }
        return address;
    }

    private void clearDefaults(Long userId) {
        Address reset = new Address();
        reset.setIsDefault(0);
        reset.setUpdatedAt(LocalDateTime.now());
        addressMapper.update(reset, new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1));
    }
}
