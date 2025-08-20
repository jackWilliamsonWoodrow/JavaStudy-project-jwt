package com.test.controller;

import com.test.entity.RestBean;
import com.test.entity.dto.Account;
import com.test.entity.dto.AccountDetails;
import com.test.entity.vo.request.ChangePasswordVo;
import com.test.entity.vo.request.DetailsSaveVo;
import com.test.entity.vo.request.ModifyEmailVo;
import com.test.entity.vo.request.PrivacySavaVo;
import com.test.entity.vo.response.AccountDetailsVo;
import com.test.entity.vo.response.AccountPrivacyVo;
import com.test.entity.vo.response.AccountVo;
import com.test.service.AccountDetailsService;
import com.test.service.AccountPrivacyService;
import com.test.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/user")
public class AccountController {
    @Resource
    AccountService accountService;

    @Resource
    AccountPrivacyService accountPrivacyService;
    @Resource
    AccountDetailsService accountDetailsService;
    @GetMapping("/info")
    public RestBean<AccountVo> info(@RequestAttribute("id") int id){
        Account account = accountService.findAccountById(id);
        return RestBean.success(account.asViewObject(AccountVo.class));
    }

    @GetMapping("/details")
    public RestBean<AccountDetailsVo> details(@RequestAttribute("id") int id){
        AccountDetails details = Optional
                .ofNullable(accountDetailsService.findAccountDetailsById(id))
                .orElseGet(AccountDetails::new);
        return RestBean.success(details.asViewObject(AccountDetailsVo.class));
    }

    @PostMapping("/save-details")
    public RestBean<Void> saveDetails(@RequestAttribute("id") int id,
                                      @RequestBody @Valid DetailsSaveVo vo){
        boolean success = accountDetailsService.saveAccountDetails(id,vo);
        return success ? RestBean.success() : RestBean.failure(400,"此用户名已被其他用户注册，请更换！");
    }

    @PostMapping("/modify-email")
    public RestBean<Void> modifyEmail(@RequestAttribute("id") int id,
                                      @RequestBody @Valid ModifyEmailVo vo){
        String result = accountService.modifyEmail(id, vo);
        return result == null ? RestBean.success() : RestBean.failure(400,result);

    }

    @PostMapping("/change-password")
    public RestBean<Void> changePassword(@RequestAttribute("id") int id,
                                         @RequestBody @Valid ChangePasswordVo vo){
        return this.messageHandle(() -> accountService.changePassword(id, vo));
    }

    @PostMapping("/save-privacy")
    public RestBean<Void> savePrivacy(@RequestAttribute("id") int id,
                                      @RequestBody @Valid PrivacySavaVo vo){
        accountPrivacyService.savePrivacy(id, vo);
        return RestBean.success();
    }

    @GetMapping("/privacy")
    public RestBean<AccountPrivacyVo> savePrivacy(@RequestAttribute("id") int id){

        return RestBean.success(accountPrivacyService.accountPrivacy(id).asViewObject(AccountPrivacyVo.class));

    }


    private RestBean<Void> messageHandle(Supplier<String> action){
        String message = action.get();
        return message == null ? RestBean.success() : RestBean.failure(400,message);
    }
}
