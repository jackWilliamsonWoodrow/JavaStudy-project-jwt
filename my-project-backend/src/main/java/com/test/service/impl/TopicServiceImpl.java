package com.test.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.entity.dto.Topic;
import com.test.entity.dto.TopicType;
import com.test.entity.vo.request.TopicCreateVo;
import com.test.entity.vo.response.TopicPreviewVo;
import com.test.mapper.TopicMapper;
import com.test.mapper.TopicTypeMapper;
import com.test.service.TopicService;
import com.test.utils.CacheUtils;
import com.test.utils.Const;
import com.test.utils.FlowUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService{

    @Resource
    TopicTypeMapper topicTypeMapper;

    @Resource
    FlowUtils flowUtils;
    @Resource
    CacheUtils cacheUtils;
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
            cacheUtils.deleteCache(Const.FORUM_TOPIC_PREVIEW_CACHE+"*");
            // 保存成功，返回null表示操作成功
            return null;
        }else {
            // 保存失败，返回错误信息
            return "内部错误，请联系管理员";
        }
    }

    /**
     * 分页获取主题列表预览信息
     * 根据类型筛选主题，并转换为前端所需的预览格式
     *
     * @param page 页码（从0开始）
     * @param type 主题类型（0表示所有类型，其他值表示特定类型）
     * @return 主题预览列表，如果没有数据返回null
     */
    @Override
    public List<TopicPreviewVo> listTopicByPage(int page, int type) {
        String key = Const.FORUM_TOPIC_PREVIEW_CACHE +page+ ":" +type;
        List<TopicPreviewVo> list = cacheUtils.takeListFromCache(key, TopicPreviewVo.class);
        if (list != null) return list;
        List<Topic> topics;

        // 根据类型选择不同的查询方式
        if (type == 0){
            // 查询所有类型的主题，每页10条
            topics = baseMapper.topicList(page * 10);
        }else {
            // 查询指定类型的主题，每页10条
            topics = baseMapper.topicListByType(page * 10, type);
        }

        // 如果查询结果为空，直接返回null
        if(topics.isEmpty()) return null;

        // 将Topic实体列表转换为TopicPreviewVo预览列表
        list = topics.stream().map(this::resolveToPreview).toList();
        cacheUtils.saveListToCache(key,list,60);


        // 问题：这里错误地返回了null，应该返回list
        return list; // 这里应该是 return list;
    }

    /**
     * 将Topic实体转换为前端预览使用的VO对象
     * 提取文本预览和图片列表
     *
     * @param topic 主题实体对象
     * @return 主题预览值对象
     */
    private TopicPreviewVo resolveToPreview(Topic topic){
        // 创建预览VO对象并复制基本属性
        TopicPreviewVo vo = new TopicPreviewVo();
        BeanUtils.copyProperties(topic, vo);

        // 用于存储主题中的图片URL列表
        List<String> images = new ArrayList<>();
        // 用于构建文本预览内容
        StringBuilder previewText = new StringBuilder();

        // 解析JSON格式的内容（假设使用Quill编辑器格式）
        // 格式示例：{"ops":[{"insert":"文本内容"},{"insert":{"image":"url"}}]}
        JSONArray ops = JSONObject.parseObject(topic.getContent()).getJSONArray("ops");

        // 遍历内容中的每个操作（operation）
        for (Object op : ops){
            // 获取insert字段的内容
            Object insert = JSONObject.from(op).get("insert");

            if (insert instanceof String text){
                // 如果是文本内容，添加到预览文本中
                // 限制预览文本长度不超过300字符
                if (previewText.length() >= 300) continue;
                previewText.append(text);
            } else if (insert instanceof Map<?, ?> map) {
                // 如果是Map类型，可能是图片或其他嵌入内容
                // 检查是否存在image字段
                Optional.ofNullable(map.get("image"))
                        .ifPresent(obj -> images.add(obj.toString()));
            }
        }

        // 设置预览文本，确保不超过300字符
        vo.setText(previewText.length() > 300 ?
                previewText.substring(0, 300) :
                previewText.toString());

        // 设置图片列表
        vo.setImages(images);

        return vo;
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
