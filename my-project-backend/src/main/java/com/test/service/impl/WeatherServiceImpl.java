package com.test.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.test.entity.vo.response.WeatherVo;
import com.test.service.WeatherService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

@Service
public class WeatherServiceImpl implements WeatherService {

    @Value("${spring.weather.key}")
    String key;
    @Resource
    RestTemplate restTemplate;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Override
    public WeatherVo fetchWeather(double longitude, double latitude) {
        return fetchFromCache(longitude, latitude);
    }

    private WeatherVo fetchFromCache(double longitude,double latitude){
        JSONObject geo = this.decompressStringToJson(restTemplate.getForObject(
                "https://pa33jnxqqt.re.qweatherapi.com/geo/v2/city/lookup?location="+longitude+","+latitude+"&X-QW-Api-Key="+key,byte[].class));
        if (geo == null) return null;
        JSONObject location = geo.getJSONObject("location");
        int id = location.getInteger("id");
        String key = "weather:"+id;
        String cache = stringRedisTemplate.opsForValue().get(key);
        if (cache != null)
            return JSONObject.parseObject(cache).to(WeatherVo.class);
        WeatherVo vo = this.fetchFromAPI(id, location);
        if (vo == null) return null;
        stringRedisTemplate.opsForValue().set(key,JSONObject.from(vo).toJSONString(),1, TimeUnit.HOURS);
        return vo;
    }

    private WeatherVo fetchFromAPI(int id, JSONObject location){
        WeatherVo vo = new WeatherVo();
        vo.setLocation(location);
        JSONObject now = this.decompressStringToJson(restTemplate.getForObject(
                "https://pa33jnxqqt.re.qweatherapi.com/v7/weather/now?location="+id+"&X-QW-Api-Key="+key,byte[].class
        ));
        if (now == null) return null;
        vo.setNow(now.getJSONObject("now"));
        JSONObject hourly = this.decompressStringToJson(restTemplate.getForObject(
                "https://pa33jnxqqt.re.qweatherapi.com/v7/weather/{24h}?location="+id+"&X-QW-Api-Key="+key,byte[].class
        ));
        if (hourly == null) return null;
        vo.setHourly(new JSONArray(hourly.getJSONArray("hourly").stream().limit(5).toList()));
        return vo;


    }

    private JSONObject decompressStringToJson(byte[] data){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try{
            GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data));
            byte[] buffer = new byte[1024];
            int read;
            while ((read = gzip.read(buffer))!= -1)
                stream.write(buffer,0,read);
            gzip.close();
            stream.close();
            return JSONObject.parseObject(stream.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
