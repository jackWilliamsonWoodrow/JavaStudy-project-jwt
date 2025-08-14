package com.test.entity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.entity.dto.Account;
import com.test.entity.service.AccountService;
import com.test.mapper.AccountMapper;
import com.test.utils.Const;
import com.test.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    //根据用户名加载用户详细资料：根据用户名查找用户信息并转换为 Spring Security 可识别的 UserDetails 对象
    //参数：username - 用户提供的用户名或邮箱
    //返回值：UserDetails - 包含用户认证信息的接口实现

    @Resource
    AmqpTemplate amqpTemplate;
    @Resource
    FlowUtils utils;

    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByNameOrEmail(username);
        if (account == null)
            throw new UsernameNotFoundException("用户名或密码错误");
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }
    public Account findAccountByNameOrEmail(String text){
        return this.query()
                .eq("username",text).or()
                .eq("email",text)
                .one();
    }

    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()){
            if (!this.verifyLimit(ip))
                return "请求频发，请稍后再试";
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            Map<String, Object> data = Map.of("type",type,"email",email,"code",code);    //type可能是register（注册）或者reset（重置密码）
            amqpTemplate.convertAndSend("mail",data);                                           //消费数据（验证码）
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA+email,String.valueOf(code),3, TimeUnit.MINUTES);
            return null;
        }
    }
//（限流）限制用户单次请求次数，如果此用户已经发送过邮件，redis中有此数据，则返回false，
// 如何没有则在redis中添加此用户ip，并设置过期时间为60s
    private boolean verifyLimit(String ip){
        String key = Const.VERIFY_EMAIL_LIMIT+ ip;
        return utils.limitOnceCheck(key,60);
    }
}
