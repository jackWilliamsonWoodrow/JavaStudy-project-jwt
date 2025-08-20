package com.test.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.test.entity.dto.Account;
import com.test.entity.vo.request.ConfirmResetVo;
import com.test.entity.vo.request.EmailRegisterVo;
import com.test.entity.vo.request.EmailResetVo;
import com.test.entity.vo.request.ModifyEmailVo;
import com.test.entity.vo.request.ChangePasswordVo;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.net.UnknownHostException;

public interface AccountService extends IService<Account>, UserDetailsService {
    Account findAccountByNameOrEmail(String text);
    Account findAccountById(int id);

    String registerEmailVerifyCode(String type,String email,String ip);
    String registerEmailAccount(EmailRegisterVo emailRegisterVo);
    String resetConfirm(ConfirmResetVo vo);
    String resetEmailAccountPassword(EmailResetVo vo);
    String modifyEmail(int id, ModifyEmailVo vo);

    String changePassword(int id, ChangePasswordVo vo);


}
