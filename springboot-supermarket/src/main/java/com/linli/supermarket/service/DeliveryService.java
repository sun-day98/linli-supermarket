package com.linli.supermarket.service;

import com.linli.supermarket.dto.DeliveryAreaDTO;
import com.linli.supermarket.entity.Deliveryman;
import com.linli.supermarket.vo.DeliveryAreaVO;
import java.util.List;

/** 配送区域与配送员服务。 */
public interface DeliveryService {

    /** 启用区域树（仅城市及其下启用区县）。 */
    List<DeliveryAreaVO> treeEnabled();

    /** 全部区域树（管理端）。 */
    List<DeliveryAreaVO> treeAll();

    Long createArea(DeliveryAreaDTO dto);

    void updateArea(Long id, DeliveryAreaDTO dto);

    void deleteArea(Long id);

    /** 按区县名判断可配送（区县启用且所属城市启用）。 */
    boolean deliverable(String district);

    List<Deliveryman> listDeliverymen();

    Long createDeliveryman(Deliveryman deliveryman);

    void updateDeliveryman(Long id, Deliveryman deliveryman);

    void deleteDeliveryman(Long id);
}
