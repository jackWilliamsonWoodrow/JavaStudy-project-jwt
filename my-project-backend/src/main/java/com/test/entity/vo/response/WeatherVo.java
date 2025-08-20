package com.test.entity.vo.response;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import netscape.javascript.JSObject;

@Data
public class WeatherVo {
    JSONObject location;
    JSONObject now;
    JSONArray hourly;
}
