package com.sunyongjie.blog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求。
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "不能为空")
    @Size(min = 3, max = 50, message = "长度需在 3-50 之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "不能为空")
    @Size(min = 6, max = 50, message = "长度需在 6-50 之间")
    private String password;

    @NotBlank(message = "不能为空")
    @Size(max = 50, message = "长度不能超过 50")
    private String nickname;

    @Email(message = "格式不正确")
    @Size(max = 100, message = "长度不能超过 100")
    private String email;
}
