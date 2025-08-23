package com.test.entity.vo.response;

import lombok.Data;

import java.util.Date;

@Data
public class TopicDetailVo {
    Integer id;
    String title;
    String content;
    Integer type;
    Date time;
    User user;

    @Data
    public static class User{
        Integer id;
        String username;
        String avatar;
        String desc;
        boolean gender;
        String qq;
        String wx;
        String phone;
        String email;
    }
}
