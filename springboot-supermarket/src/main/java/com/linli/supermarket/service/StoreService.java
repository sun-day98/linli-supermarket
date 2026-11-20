package com.linli.supermarket.service;

import com.linli.supermarket.entity.StoreSetting;

/** 门店设置服务（单行 id=1）。 */
public interface StoreService {

    StoreSetting get();

    void update(StoreSetting setting);
}
