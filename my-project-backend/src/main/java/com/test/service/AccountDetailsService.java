package com.test.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.test.entity.dto.AccountDetails;
import com.test.entity.vo.request.DetailsSaveVo;

public interface AccountDetailsService extends IService<AccountDetails> {
    AccountDetails findAccountDetailsById(int id);
    boolean saveAccountDetails(int id, DetailsSaveVo vo);
}
