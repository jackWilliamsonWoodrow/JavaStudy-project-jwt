package com.test.controller;

import com.test.entity.RestBean;
import com.test.entity.vo.request.ConfirmResetVo;
import com.test.entity.vo.request.EmailRegisterVo;
import com.test.entity.vo.request.EmailResetVo;
import com.test.service.AccountService;
import com.test.utils.ControllerUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {

    @Resource
    AccountService service;

    @Resource
    ControllerUtils utils;

    //申请验证码
    @GetMapping("/ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam @Email String email,
                                        @RequestParam @Pattern(regexp = "(register|reset|modify)") String type,
                                        HttpServletRequest request){
        return utils.messageHandle(() ->
        service.registerEmailVerifyCode(type,email,request.getRemoteAddr()));
    }

    @PostMapping("/register")
    public RestBean<Void> register(@RequestBody @Valid EmailRegisterVo vo){
        return utils.messageHandle(() ->
                service.registerEmailAccount(vo));
    }


    @PostMapping("/reset-confirm")
    public RestBean<Void> resetConfirm(@RequestBody @Valid ConfirmResetVo vo){
        return utils.messageHandle(() -> service.resetConfirm(vo));
    }

    @PostMapping("/reset-password")
    public RestBean<Void> resetPassword(@RequestBody @Valid EmailResetVo vo){
        return utils.messageHandle(() -> service.resetEmailAccountPassword(vo));
    }

}
