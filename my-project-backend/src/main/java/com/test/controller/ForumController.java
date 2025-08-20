package com.test.controller;

import com.test.entity.RestBean;
import com.test.entity.vo.response.WeatherVo;
import com.test.service.WeatherService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forum")
public class ForumController {
    @Resource
    WeatherService service;
    @GetMapping("/weather")
    public RestBean<WeatherVo> weather(double longitude, double latitude){
        WeatherVo weatherVo = service.fetchWeather(longitude, latitude);
        return weatherVo == null ?
                RestBean.failure(400,"获取地理位置信息失败，请联系管理员！"): RestBean.success(weatherVo);
    }
}
