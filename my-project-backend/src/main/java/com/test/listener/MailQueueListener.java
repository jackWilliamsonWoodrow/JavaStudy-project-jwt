package com.test.listener;

import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = "mail")
public class MailQueueListener {
    @Resource
    JavaMailSender sender;

    @Value("${spring.mail.username}")
    String username;
//发送邮件根据redis中的内容获得信息
    @RabbitHandler
    public void sendMailMessage(Map<String, Object> data){
        String email = (String) data.get("email");
        Integer code = (Integer) data.get("code");
        String type = (String) data.get("type");
        SimpleMailMessage message = switch (type) {
            case "register" ->
                createMessage("欢迎注册我们的网站","您的邮件注册验证码是："+code+",有限时间3分钟，为保障您的安全，请勿向他人泄露验证码信息。",email);
            case "reset" -> createMessage("你的密码重置邮件",
                    "您好，您正在进行重置密码操作，验证码为："+code+",有效时间3分钟，如非本人操作请无视。",email);
            case "modify" ->
                createMessage("您的邮件修改验证邮件",
                        "您好，您正在进行重置邮件操作，验证码为："+code+",有效时间3分钟，如非本人操作请无视。",email);
            default -> null;
        };
        if (message == null) return;
        sender.send(message);
    }

    //设置邮件格式
    private SimpleMailMessage createMessage(String title,String content,String email){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(title);
        message.setText(content);
        message.setTo(email);
        message.setFrom(username);
        return message;

    }


}
