package com.test.controller;

import com.test.entity.RestBean;
import com.test.entity.dto.Account;
import com.test.entity.vo.response.AccountVo;
import com.test.service.AccountService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class AccountController {
    @Resource
    AccountService accountService;
    @GetMapping("/info")
    public RestBean<AccountVo> info(@RequestAttribute("id") int id){
        Account account = accountService.findAccountById(id);
        return RestBean.success(account.asViewObject(AccountVo.class));
    }
}
