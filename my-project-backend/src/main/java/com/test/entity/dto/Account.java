package com.test.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.Date;
//DTO（Data Transfer Object）：主要用于在展示层与服务层之间传输数据，不包含业务逻辑。
// DTO的作用是确保数据在不同系统或组件间的准确无误传递。例如，当展示层需要向服务层请求数据时，会将请求数据封装进DTO中，以标准化格式发送请求。
@Data
@TableName("db_account")
@AllArgsConstructor
public class Account {
    @TableId(type = IdType.AUTO)
    Integer id;
    String username;
    String password;
    String email;
    String role;
    Date registerTime;
}
