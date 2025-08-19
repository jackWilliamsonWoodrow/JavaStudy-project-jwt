package com.test.entity.vo.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class DetailsSaveVo {
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]{3,16}$")
    @Length(min = 3,max = 16)
    String username;
    @Min(0)
    @Max(1)
    int gender;
    @Length(max = 11)
    String phone;
    @Length(max = 13)
    String qq;
    @Length(max = 20)
    String wx;
    @Length(max = 200)
    String desc;
}
