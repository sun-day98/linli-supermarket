package com.linli.supermarket.service;

import com.linli.supermarket.dto.AddressDTO;
import com.linli.supermarket.entity.Address;
import java.util.List;

/** 收货地址服务。 */
public interface AddressService {

    List<Address> list(Long userId);

    Long create(Long userId, AddressDTO dto);

    void update(Long userId, Long id, AddressDTO dto);

    void delete(Long userId, Long id);

    void setDefault(Long userId, Long id);

    /** 查询属于指定用户的地址，不存在时抛业务异常。 */
    Address getOwned(Long userId, Long id);
}
