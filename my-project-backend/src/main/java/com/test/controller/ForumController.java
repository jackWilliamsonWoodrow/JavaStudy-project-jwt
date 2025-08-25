package com.test.controller;

import com.test.entity.RestBean;
import com.test.entity.dto.Interact;
import com.test.entity.vo.request.TopicCreateVo;
import com.test.entity.vo.request.TopicUpdateVo;
import com.test.entity.vo.response.*;
import com.test.service.TopicService;
import com.test.service.WeatherService;
import com.test.utils.ControllerUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/forum")
public class ForumController {
    @Resource
    WeatherService service;

    @Resource
    ControllerUtils utils;
    @Resource
    TopicService topicService;
    @GetMapping("/weather")
    public RestBean<WeatherVo> weather(double longitude, double latitude){
        WeatherVo weatherVo = service.fetchWeather(longitude, latitude);
        return weatherVo == null ?
                RestBean.failure(400,"获取地理位置信息失败，请联系管理员！"): RestBean.success(weatherVo);
    }

    @GetMapping("/types")
    public RestBean<List<TopicTypeVo>> listTypes(){
        return RestBean.success(topicService
                .listTypes()
                .stream()
                .map(type -> type.asViewObject(TopicTypeVo.class))
                .toList());
    }

    @PostMapping("/create-topic")
    public RestBean<Void> createTopic(@Valid @RequestBody TopicCreateVo vo,
                                       @RequestAttribute("id") int id ){
        return utils.messageHandle(() -> topicService.createTopic(id,vo));

    }

    @GetMapping("/list-topic")
    public RestBean<List<TopicPreviewVo>> listTopic(@RequestParam @Min(0) int page,
                                                    @RequestParam @Min(0) int type){
        return RestBean.success(topicService.listTopicByPage(page+1,type));
    }

    @GetMapping("/top-topic")
    public RestBean<List<TopicTopVo>> topTopic(){
        return RestBean.success(topicService.listTopTopic());
    }

    @GetMapping("/topic")
    public RestBean<TopicDetailVo> topic(@RequestParam @Min(0) int tid,
                                         @RequestAttribute("id") int id ){
        return RestBean.success(topicService.getTopic(tid,id));
    }
    @GetMapping("/interact")
    public RestBean<Void> interact(@RequestParam @Min(0) int tid,
                                   @RequestParam @Pattern(regexp = "(like|collect)") String type,
                                   @RequestParam boolean state,
                                   @RequestAttribute("id") int id){
        topicService.interact(new Interact(tid,id,new Date(),type),state);
        return RestBean.success();
    }

    @GetMapping("/collects")
    public RestBean<List<TopicPreviewVo>> collects(@RequestAttribute("id") int id){
        return RestBean.success(topicService.listTopicCollects(id));
    }

    @PostMapping("/update-topic")
    public RestBean<Void> updateTopic(@Valid @RequestBody TopicUpdateVo vo,
                                      @RequestAttribute("id") int id){
        return utils.messageHandle(() -> topicService.updateTopic(id,vo));
    }
}
