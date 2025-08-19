package com.test.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.test.entity.dto.AccountPrivacy;
import com.test.entity.vo.request.PrivacySavaVo;

public interface AccountPrivacyService extends IService<AccountPrivacy> {
    void savePrivacy(int id, PrivacySavaVo vo);

    AccountPrivacy accountPrivacy(int id);
}
