package com.linli.supermarket.service.impl;

import com.linli.supermarket.entity.StoreSetting;
import com.linli.supermarket.mapper.StoreSettingMapper;
import com.linli.supermarket.service.StoreService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 门店设置服务实现（固定单行 id=1）。 */
@Service
public class StoreServiceImpl implements StoreService {

    @Autowired
    private StoreSettingMapper storeSettingMapper;

    @Override
    public StoreSetting get() {
        StoreSetting setting = storeSettingMapper.selectById(1L);
        if (setting != null) {
            return setting;
        }
        StoreSetting fallback = new StoreSetting();
        fallback.setId(1L);
        fallback.setStoreName("邻里小超·社区优选");
        fallback.setSlogan("新鲜到家，邻里共享");
        fallback.setNotice("本店支持当日达，满59元免配送费");
        fallback.setDeliveryFee(new BigDecimal("5.00"));
        fallback.setFreeDeliveryThreshold(new BigDecimal("59.00"));
        fallback.setServicePhone("400-800-1234");
        fallback.setLogo("");
        return fallback;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(StoreSetting setting) {
        setting.setId(1L);
        setting.setUpdatedAt(LocalDateTime.now());
        if (storeSettingMapper.selectById(1L) == null) {
            setting.setCreatedAt(LocalDateTime.now());
            storeSettingMapper.insert(setting);
        } else {
            storeSettingMapper.updateById(setting);
        }
    }
}
