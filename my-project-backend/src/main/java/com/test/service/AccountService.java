package com.test.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.test.entity.dto.Account;
import com.test.entity.vo.request.EmailRegisterVo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<Account>, UserDetailsService {
    Account findAccountByNameOrEmail(String text);

    String registerEmailVerifyCode(String type,String email,String ip);
    String registerEmailAccount(EmailRegisterVo emailRegisterVo);

}
