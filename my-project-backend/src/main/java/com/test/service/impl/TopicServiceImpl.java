package com.test.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.entity.dto.*;
import com.test.entity.vo.request.TopicCreateVo;
import com.test.entity.vo.response.TopicDetailVo;
import com.test.entity.vo.response.TopicPreviewVo;
import com.test.entity.vo.response.TopicTopVo;
import com.test.mapper.*;
import com.test.service.TopicService;
import com.test.utils.CacheUtils;
import com.test.utils.Const;
import com.test.utils.FlowUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService{

    @Resource
    TopicTypeMapper topicTypeMapper;

    @Resource
    FlowUtils flowUtils;
    @Resource
    CacheUtils cacheUtils;

    @Resource
    AccountMapper accountMapper;
    @Resource
    AccountDetailsMapper accountDetailsMapper;
    @Resource
    AccountPrivacyMapper accountPrivacyMapper;
    @Resource
    StringRedisTemplate stringRedisTemplate;
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
            cacheUtils.deleteCachePattern(Const.FORUM_TOPIC_PREVIEW_CACHE+"*");
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
     * @param pageNumber 页码（从1开始）
     * @param type 主题类型（0表示所有类型，其他值表示特定类型）
     * @return 主题预览列表，如果没有数据返回null
     */
    @Override
    public List<TopicPreviewVo> listTopicByPage(int pageNumber, int type) {
        String key = Const.FORUM_TOPIC_PREVIEW_CACHE +pageNumber+ ":" +type;
        List<TopicPreviewVo> list = cacheUtils.takeListFromCache(key, TopicPreviewVo.class);
        if (list != null) return list;
        Page<Topic> page = Page.of(pageNumber,10);

        // 根据类型选择不同的查
        // 询方式
        if (type == 0){
            // 查询所有类型的主题，每页10条
            baseMapper.selectPage(page,Wrappers.<Topic>query().orderByDesc("time"));
        }else {
            // 查询指定类型的主题，每页10条
            baseMapper.selectPage(page,Wrappers.<Topic>query().eq("type",type).orderByDesc("time"));
        }

        List<Topic> topics = page.getRecords();
        // 如果查询结果为空，直接返回null
        if(topics.isEmpty()) return null;

        // 将Topic实体列表转换为TopicPreviewVo预览列表
        list = topics.stream().map(this::resolveToPreview).toList();
        cacheUtils.saveListToCache(key,list,60);


        // 问题：这里错误地返回了null，应该返回list
        return list; // 这里应该是 return list;
    }

    @Override
    public List<TopicTopVo> listTopTopic() {
        List<Topic> topics = baseMapper.selectList(Wrappers.<Topic>query()
                .select("id","title","time")
                .eq("top",1));
        return topics.stream().map(topic -> {
            TopicTopVo vo = new TopicTopVo();
            BeanUtils.copyProperties(topic,vo);
            return vo;
        }).toList();
    }
//获取帖子详情
    @Override
    public TopicDetailVo getTopic(int tid) {
        TopicDetailVo vo = new TopicDetailVo();
        Topic topic = baseMapper.selectById(tid);
        BeanUtils.copyProperties(topic,vo);
        TopicDetailVo.User user = new TopicDetailVo.User();
        vo.setUser(this.fillUserDetailsByPrivacy(user,topic.getUid()));
        return vo;
    }

    /**
     * 用户互动操作处理服务
     */
    @Override
    public void interact(Interact interact, boolean state) {
        String type = interact.getType();

        // 使用字符串驻留实现基于类型的细粒度锁，避免不同类型操作之间的锁竞争
        synchronized (type.intern()) {
            // 将互动状态存储到Redis哈希表中，key为互动类型，field为互动唯一标识，value为状态
            stringRedisTemplate.opsForHash().put(type, interact.toKey(), Boolean.toString(state));

            // 触发定时保存任务
            this.saveInteractSchedule(type);
        }
    }

    // 用于跟踪各类型互动是否已经安排了保存任务的状态映射
    private final Map<String, Boolean> state = new HashMap<>();

    // 创建定时任务线程池，用于延迟执行数据库保存操作
    ScheduledExecutorService service = Executors.newScheduledThreadPool(2);

    /**
     * 安排互动数据的延迟保存任务
     * @param type 互动类型
     */
    private void saveInteractSchedule(String type) {
        // 检查该类型是否已经安排了保存任务，避免重复安排
        if (!state.getOrDefault(type, false)) {
            // 标记该类型已有保存任务安排
            state.put(type, true);

            // 安排一个延迟3秒执行的任务，实现批量操作的缓冲效果
            service.schedule(() -> {
                // 执行实际的数据库保存操作
                this.saveInteract(type);

                // 重置该类型的任务状态，允许新的保存任务安排
                state.put(type, false);
            }, 3, TimeUnit.SECONDS);
        }
    }

    /**
     * 将Redis中的互动数据保存到数据库
     * @param type 互动类型
     */
    private void saveInteract(String type) {
        // 再次加锁确保线程安全
        synchronized (type.intern()) {
            // 创建列表分别存储要点赞/取消点赞的数据
            List<Interact> check = new LinkedList<>();
            List<Interact> unCheck = new LinkedList<>();

            // 从Redis哈希表中获取该类型的所有互动数据
            stringRedisTemplate.opsForHash().entries(type).forEach((k, v) -> {
                // 根据状态值分类处理
                if (Boolean.parseBoolean(v.toString())) {
                    // 状态为true的互动（如点赞）
                    check.add(Interact.parseInteract(k.toString(), type));
                } else {
                    // 状态为false的互动（如取消点赞）
                    unCheck.add(Interact.parseInteract(k.toString(), type));
                }
            });

            // 批量处理要点赞的互动数据
            if (!check.isEmpty())
                baseMapper.addInteract(check, type);

            // 批量处理要取消点赞的互动数据
            if (!unCheck.isEmpty())
                baseMapper.deleteInteract(unCheck, type); // 注意：这里应该是unCheck而不是check

            // 清空Redis中该类型的临时数据
            stringRedisTemplate.delete(type);
        }
    }

    private <T> T fillUserDetailsByPrivacy(T target,int uid){
        AccountDetails details = accountDetailsMapper.selectById(uid);
        Account account = accountMapper.selectById(uid);
        AccountPrivacy accountPrivacy = accountPrivacyMapper.selectById(uid);
        String[] ignores = accountPrivacy.hiddenFields();
        BeanUtils.copyProperties(account,target,ignores);
        BeanUtils.copyProperties(details,target,ignores);
        return target;
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
        //用户的信息单独再从用户表查一遍
        BeanUtils.copyProperties(accountMapper.selectById(topic.getUid()),vo);

        //帖子的信息在帖子表里查
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
