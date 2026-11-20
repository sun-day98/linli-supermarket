package com.linli.supermarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 优惠券新增/编辑请求（管理端）。 */
@Data
public class CouponDTO {

    @NotBlank(message = "券名不能为空")
    private String name;

    @NotNull(message = "券类型不能为空")
    private Integer type;

    private BigDecimal discount;
    private BigDecimal threshold;
    private Integer validType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer validDays;
    private Integer scope;
    private Integer total;
    private Integer newUserAuto;
    private Integer status;
}
