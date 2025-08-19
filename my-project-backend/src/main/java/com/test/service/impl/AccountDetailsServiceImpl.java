package com.test.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.entity.dto.Account;
import com.test.entity.dto.AccountDetails;
import com.test.entity.vo.request.DetailsSaveVo;
import com.test.mapper.AccountDetailsMapper;
import com.test.service.AccountDetailsService;
import com.test.service.AccountService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AccountDetailsServiceImpl extends ServiceImpl<AccountDetailsMapper, AccountDetails> implements AccountDetailsService {

    @Resource
    AccountService accountService;
    @Override
    public AccountDetails findAccountDetailsById(int id) {
        return this.getById(id);
    }
//同步更新account表中的数据，并保存或更新account_details表
    @Override
    public synchronized boolean saveAccountDetails(int id, DetailsSaveVo vo) {
        Account account = accountService.findAccountByNameOrEmail(vo.getUsername());
        if (account == null || account.getId() == id){                            //数据库中已有的名字不能重复，或者是用户本人使用之前的名字
            accountService.update()
                    .eq("id",id)
                    .set("username", vo.getUsername())
                    .update();
            this.saveOrUpdate(new AccountDetails(
                    id, vo.getGender(), vo.getPhone(),vo.getQq(),vo.getWx(),vo.getDesc()
            ));
            return true;
        }
        return false;
    }
}
