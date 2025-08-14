package com.test.entity.vo.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class EmailRegisterVo {
    @Email
    String mail;
    @Length(max = 6,min = 6)
    String code;
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]{3,16}$")
    @Length(min = 3,max = 16)
    String username;
    @Length(min = 6,max =20)
    String password;
}
