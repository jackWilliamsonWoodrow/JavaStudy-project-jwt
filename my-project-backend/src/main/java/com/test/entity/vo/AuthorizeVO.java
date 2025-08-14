package com.test.entity.vo;

import lombok.Data;

import java.util.Date;
//vo(view object)视图对象，与前端交互所使用的对象
@Data
public class AuthorizeVO {
    String username;
    String role;
    String token;;
    Date expire;
}
