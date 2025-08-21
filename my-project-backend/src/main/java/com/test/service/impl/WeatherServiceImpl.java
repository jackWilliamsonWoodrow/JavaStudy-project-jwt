package com.test.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.test.entity.vo.response.WeatherVo;
import com.test.service.WeatherService;
import com.test.utils.Const;
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
    String key; // API密钥
    @Resource
    RestTemplate restTemplate; // HTTP请求模板
    @Resource
    StringRedisTemplate stringRedisTemplate; // Redis操作模板

    @Override
    public WeatherVo fetchWeather(double longitude, double latitude) {
        // 获取天气信息（从缓存或API）
        return fetchFromCache(longitude, latitude);
    }

    /**
     * 从缓存获取天气信息，不存在时从API获取并缓存
     */
    private WeatherVo fetchFromCache(double longitude, double latitude){
        // 1. 根据经纬度获取城市信息
        JSONObject geo = this.decompressStringToJson(restTemplate.getForObject(
                "https://pa33jnxqqt.re.qweatherapi.com/geo/v2/city/lookup?location="+longitude+","+latitude+"&key="+key, byte[].class));
        if (geo == null) return null; // API调用失败

        // 2. 解析位置信息获取城市ID
        JSONObject location = geo.getJSONArray("location").getJSONObject(0);
        int id = location.getInteger("id");
        String key = Const.FORUM_WEATHER_CACHE +id; // 构造Redis键

        // 3. 检查Redis缓存
        String cache = stringRedisTemplate.opsForValue().get(key);
        if (cache != null)
            return JSONObject.parseObject(cache).to(WeatherVo.class); // 返回缓存数据

        // 4. 缓存不存在，从API获取
        WeatherVo vo = this.fetchFromAPI(id, location);
        if (vo == null) return null;

        // 5. 将结果缓存到Redis，有效期1小时
        stringRedisTemplate.opsForValue().set(key, JSONObject.from(vo).toJSONString(), 1, TimeUnit.HOURS);
        return vo;
    }

    /**
     * 从天气API获取实时天气和小时预报
     */
    private WeatherVo fetchFromAPI(int id, JSONObject location){
        WeatherVo vo = new WeatherVo();
        vo.setLocation(location); // 设置位置信息

        // 获取实时天气
        JSONObject now = this.decompressStringToJson(restTemplate.getForObject(
                "https://pa33jnxqqt.re.qweatherapi.com/v7/weather/now?location="+id+"&key="+key, byte[].class
        ));
        if (now == null) return null; // API调用失败
        vo.setNow(now.getJSONObject("now"));

        // 获取24小时预报（只取前5小时）
        JSONObject hourly = this.decompressStringToJson(restTemplate.getForObject(
                "https://pa33jnxqqt.re.qweatherapi.com/v7/weather/24h?location="+id+"&key="+key, byte[].class
        ));
        if (hourly == null) return null; // API调用失败
        vo.setHourly(new JSONArray(hourly.getJSONArray("hourly").stream().limit(5).toList()));

        return vo;
    }

    /**
     * 解压GZIP压缩数据并转换为JSON对象
     */
    private JSONObject decompressStringToJson(byte[] data){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try{
            GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data));
            byte[] buffer = new byte[1024];
            int read;
            while ((read = gzip.read(buffer)) != -1)
                stream.write(buffer, 0, read);
            gzip.close();
            stream.close();
            return JSONObject.parseObject(stream.toString());
        } catch (IOException e) {
            throw new RuntimeException(e); // 解压失败抛出运行时异常
        }
    }
}