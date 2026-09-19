package com.sunyongjie.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改昵称 / 头像。
 */
@Data
public class UpdateProfileRequest {

    @NotBlank(message = "不能为空")
    @Size(max = 50, message = "长度不能超过 50")
    private String nickname;

    @Size(max = 255, message = "长度不能超过 255")
    private String avatar;
}
