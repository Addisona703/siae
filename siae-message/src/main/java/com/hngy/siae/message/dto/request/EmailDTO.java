package com.hngy.siae.message.dto.request;

import com.hngy.siae.common.validation.UpdateGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class EmailDTO {
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "验证码不能为空", groups = UpdateGroup.class)
    private String code;
}
