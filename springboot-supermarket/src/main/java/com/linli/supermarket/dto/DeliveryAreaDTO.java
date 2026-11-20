package com.linli.supermarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 配送区域新增/编辑请求（管理端）。 */
@Data
public class DeliveryAreaDTO {

    /** 0=城市，&gt;0=所属城市 ID。 */
    private Long parentId = 0L;

    @NotBlank(message = "区域名称不能为空")
    private String name;

    @NotNull(message = "区域类型不能为空")
    private Integer type;

    private Integer enabled;
    private Integer hot;
    private Integer sort;
}
