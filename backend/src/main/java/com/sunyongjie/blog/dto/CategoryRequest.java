package com.sunyongjie.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增 / 修改分类。
 */
@Data
public class CategoryRequest {

    @NotBlank(message = "不能为空")
    @Size(max = 50, message = "长度不能超过 50")
    private String name;

    /** 排序值，不传按 0 处理 */
    private Integer sort;
}
