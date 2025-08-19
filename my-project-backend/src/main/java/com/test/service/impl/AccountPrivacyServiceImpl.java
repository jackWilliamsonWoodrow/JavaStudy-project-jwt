package com.test.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.entity.dto.AccountPrivacy;
import com.test.entity.vo.request.PrivacySavaVo;
import com.test.mapper.AccountPrivacyMapper;
import com.test.service.AccountPrivacyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AccountPrivacyServiceImpl extends ServiceImpl<AccountPrivacyMapper, AccountPrivacy> implements AccountPrivacyService {
/*
用于保存用户的隐私设置
AccountPrivacy 可能不存在（即 this.getById(id) 返回 null）的情况通常有以下几种：

1. 新用户首次设置隐私
当用户第一次使用系统时，还没有创建对应的 AccountPrivacy 记录

getById(id) 会返回 null，此时会创建新对象 new AccountPrivacy(id)

2. 数据库记录被删除
可能由于管理员操作或数据清理，原有的 AccountPrivacy 记录被删除
但用户ID仍然有效（存在于用户主表中）
3. 数据初始化问题
系统部署时没有初始化所有用户的隐私记录

或者数据迁移过程中部分记录丢失

根据vo.getType()的值更新不同的隐私字段
 */
    @Override
    @Transactional
    public void savePrivacy(int id, PrivacySavaVo vo) {
        AccountPrivacy privacy = Optional.ofNullable(this.getById(id)).orElse(new AccountPrivacy(id));
        boolean status = vo.isStatus();
        switch (vo.getType()){
            case "phone" -> privacy.setPhone(status);
            case "wx" -> privacy.setWx(status);
            case "qq" -> privacy.setQq(status);
            case "gender" -> privacy.setGender(status);
            case "email" -> privacy.setEmail(status);
        }
        this.saveOrUpdate(privacy);
    }

    @Override
    public AccountPrivacy accountPrivacy(int id){
        return Optional.ofNullable(this.getById(id)).orElse(new AccountPrivacy(id));
    }
}
