package com.test.service;

import com.test.entity.vo.response.WeatherVo;

public interface WeatherService {
    WeatherVo fetchWeather(double longitude,double latitude);
}
