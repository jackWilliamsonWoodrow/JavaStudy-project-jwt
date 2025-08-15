package com.test.controller;

import com.test.entity.RestBean;
import com.test.entity.vo.request.ConfirmResetVo;
import com.test.entity.vo.request.EmailRegisterVo;
import com.test.entity.vo.request.EmailResetVo;
import com.test.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {

    @Resource
    AccountService service;

    //申请验证码
    @GetMapping("/ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam @Email String email,
                                        @RequestParam @Pattern(regexp = "(register|reset)") String type,
                                        HttpServletRequest request){
        return this.messageHandle(() ->
        service.registerEmailVerifyCode(type,email,request.getRemoteAddr()));
    }

    @PostMapping("/register")
    public RestBean<Void> register(@RequestBody @Valid EmailRegisterVo vo){
        return this.messageHandle(() ->
                service.registerEmailAccount(vo));
    }


    @PostMapping("/reset-confirm")
    public RestBean<Void> resetConfirm(@RequestBody @Valid ConfirmResetVo vo){
        return this.messageHandle(() -> service.resetConfirm(vo));
    }

    @PostMapping("/reset-password")
    public RestBean<Void> resetPassword(@RequestBody @Valid EmailResetVo vo){
        return this.messageHandle(() -> service.resetEmailAccountPassword(vo));
    }
//supplier函数式接口不接收任何参数
//@FunctionalInterface
//public interface Supplier<T> {
//    T get();
//}
//返回指定类型的结果
//函数式接口，可用 lambda 表达式实现
    private RestBean<Void> messageHandle(Supplier<String> action){
        String message = action.get();
        return message == null ? RestBean.success() : RestBean.failure(400,message);
    }
}
