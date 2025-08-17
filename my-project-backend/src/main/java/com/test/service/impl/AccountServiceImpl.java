package com.test.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.entity.dto.Account;
import com.test.entity.vo.request.ConfirmResetVo;
import com.test.entity.vo.request.EmailRegisterVo;
import com.test.entity.vo.request.EmailResetVo;
import com.test.service.AccountService;
import com.test.mapper.AccountMapper;
import com.test.utils.Const;
import com.test.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
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
    @Resource
    PasswordEncoder encoder;
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
    public Account findAccountById(int id) {
        return this.query()
                .eq("id",id)
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

    @Override
    public String registerEmailAccount(EmailRegisterVo emailRegisterVo) {
        String email = emailRegisterVo.getMail();
        String username = emailRegisterVo.getUsername();
        String key = Const.VERIFY_EMAIL_DATA+email;
        System.out.println(key);
        String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA+email);
        System.out.println(code);
        if (code == null) return "请先获取验证码";
        if (!code.equals(emailRegisterVo.getCode())) return "验证码输入错误，请重新输入";
        if (this.existsAccountByUsernameOrEmail(username,email)) return "此用户名或电子邮箱已被其他用户注册!!!";
        String password = encoder.encode(emailRegisterVo.getPassword());
        Account account = new Account(null,username,password,email,"user",new Date());
        if (this.save(account)) {
            stringRedisTemplate.delete(key);
            return null;
        }else {
            return "内部错误，请联系管理员";
        }
    }
    //重置密码
    @Override
    public String resetEmailAccountPassword(EmailResetVo vo) {
        String email = vo.getEmail();
        String verify = this.resetConfirm(new ConfirmResetVo(email, vo.getCode()));
        if (verify != null) return verify;
        String password = encoder.encode(vo.getPassword());
        boolean update = this.update().eq("email",email).set("password",password).update();
        if (update){
            stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);
        }
        return null;
    }
    //验证邮箱和验证码是否匹配
    @Override
    public String resetConfirm(ConfirmResetVo vo) {
        String email = vo.getEmail();
        String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA + email);
        if (code == null) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码输入错误，请重新输入";
        return null;
    }

    /**
     * 检查邮箱是否已存在
     * @param email 要检查的邮箱地址
     * @return true-邮箱已存在，false-邮箱不存在
     * baseMapper: MyBatis-Plus自动生成的Mapper接口
     * exists(): MyBatis-Plus提供的便捷方法，用于判断符合条件的数据是否存在
     * 实际执行的SQL类似：SELECT COUNT(1) FROM account WHERE email = ? LIMIT 1
     */
    private boolean existsAccountByUsernameOrEmail(String username,String email){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email",email)
                .or()
                .eq("username",username));
    }


    //（限流）限制用户单次请求次数，如果此用户已经发送过邮件，redis中有此数据，则返回false，
// 如何没有则在redis中添加此用户ip，并设置过期时间为60s
    private boolean verifyLimit(String ip){
        String key = Const.VERIFY_EMAIL_LIMIT+ ip;
        return utils.limitOnceCheck(key,60);
    }

}
