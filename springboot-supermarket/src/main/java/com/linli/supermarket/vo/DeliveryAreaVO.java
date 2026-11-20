package com.linli.supermarket.vo;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** 配送区域树节点（城市 → 区县）。 */
@Data
public class DeliveryAreaVO {

    private Long id;
    private Long parentId;
    private String name;
    private Integer type;
    private Integer enabled;
    private Integer hot;
    private Integer sort;
    private List<DeliveryAreaVO> children = new ArrayList<>();
}
