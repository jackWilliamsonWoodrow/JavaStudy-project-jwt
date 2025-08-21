package com.test.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.entity.dto.Topic;
import com.test.entity.dto.TopicType;
import com.test.entity.vo.request.TopicCreateVo;
import com.test.mapper.TopicMapper;
import com.test.mapper.TopicTypeMapper;
import com.test.service.TopicService;
import com.test.utils.Const;
import com.test.utils.FlowUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService{

    @Resource
    TopicTypeMapper topicTypeMapper;

    @Resource
    FlowUtils flowUtils;
    private Set<Integer> types;
    @PostConstruct
    public void init() {
        // 在依赖注入完成后初始化types
        types = this.listTypes().stream().map(TopicType::getId).collect(Collectors.toSet());
    }
    @Override
    public List<TopicType> listTypes() {
        return topicTypeMapper.selectList(null);
    }


/**
 * 创建论坛主题/帖子
 * 包含内容验证、频率限制、数据预处理和保存操作
 *
 * @param uid 用户ID，表示创建主题的用户
 * @param vo 主题创建值对象，包含前端传递的创建参数
 * @return 创建成功返回null，失败返回相应的错误信息字符串
 */
    @Override
    public String createTopic(int uid, TopicCreateVo vo) {
        // 1. 内容长度检查：验证文章内容是否超过字数限制
        if (!textLimitCheck(vo.getContent()))
            return "文章内容超过字数限制！";

        // 2. 文章类型验证：检查前端传递的文章类型是否在允许的范围内
        if (!types.contains(vo.getType()))
            return "文章类型非法!!!";

        // 3. 发文频率限制：使用Redis计数器限制用户发文频率
        // Key结构：FORUM_TOPIC_CREATE_COUNTER + 用户ID
        // 限制规则：每3600秒（1小时）内最多允许创建3个主题
        String key = Const.FORUM_TOPIC_CREATE_COUNTER + uid;
        if (!flowUtils.limitPeriodCounterCheck(key, 3, 3600))
            return "发文频繁，请稍后再试";

        // 4. 数据对象转换：将VO对象属性拷贝到Entity对象
        Topic topic = new Topic();
        BeanUtils.copyProperties(vo, topic);

        // 5. 特殊字段处理：将JSON内容转换为字符串存储
        // 假设vo.getContent()返回的是JSONObject或类似结构
        topic.setContent(vo.getContent().toJSONString());

        // 6. 设置系统字段：用户ID和创建时间
        topic.setUid(uid);
        topic.setTime(new Date());

        // 7. 持久化操作：保存主题到数据库
        if (this.save(topic)){
            // 保存成功，返回null表示操作成功
            return null;
        }else {
            // 保存失败，返回错误信息
            return "内部错误，请联系管理员";
        }
    }

    private boolean textLimitCheck(JSONObject object){
        if (object == null) return false;
        long length = 0;
        for (Object op : object.getJSONArray("ops")) {
            length += JSONObject.from(op).getString("insert").length();
        }
        return length <= 20000;
    }
}
