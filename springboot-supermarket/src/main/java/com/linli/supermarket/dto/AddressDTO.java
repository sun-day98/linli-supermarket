package com.linli.supermarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 收货地址新增/编辑请求。 */
@Data
public class AddressDTO {

    @NotBlank(message = "收货人不能为空")
    private String receiver;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String phone;

    private String province;
    private String city;

    @NotBlank(message = "区县不能为空")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detail;

    private Integer isDefault;
}
