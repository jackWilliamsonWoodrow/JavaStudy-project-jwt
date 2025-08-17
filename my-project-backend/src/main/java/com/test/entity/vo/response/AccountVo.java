package com.test.entity.vo.response;

import com.test.entity.BaseData;
import lombok.Data;

import java.util.Date;

@Data
public class AccountVo{
    String username;
    String email;
    String role;
    Date registerTime;
}
