package com.linli.supermarket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 分类新增/编辑请求（管理端）。 */
@Data
public class CategoryDTO {

    @NotBlank(message = "分类名不能为空")
    private String name;

    private String icon;
    private Integer sort;
    private Integer status;
}
