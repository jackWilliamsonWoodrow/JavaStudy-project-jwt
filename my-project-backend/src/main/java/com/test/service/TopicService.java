package com.test.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.test.entity.dto.Topic;
import com.test.entity.dto.TopicType;
import com.test.entity.vo.request.TopicCreateVo;
import com.test.entity.vo.response.TopicDetailVo;
import com.test.entity.vo.response.TopicPreviewVo;
import com.test.entity.vo.response.TopicTopVo;
import com.test.entity.vo.response.TopicTypeVo;

import java.util.List;

public interface TopicService extends IService<Topic> {
    List<TopicType> listTypes();
    String createTopic(int uid, TopicCreateVo vo);
    List<TopicPreviewVo> listTopicByPage(int page,int type);
    List<TopicTopVo> listTopTopic();
    TopicDetailVo getTopic(int tid);

}
