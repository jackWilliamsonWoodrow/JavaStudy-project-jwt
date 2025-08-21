package com.test.entity.vo.request;

import com.alibaba.fastjson2.JSONObject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class TopicCreateVo {
    @Min(1)
    @Max(5)
    int type;
    @Length(min = 1,max = 40)
    String title;
    JSONObject content;
}
