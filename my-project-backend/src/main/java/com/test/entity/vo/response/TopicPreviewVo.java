package com.test.entity.vo.response;

import com.baomidou.mybatisplus.core.handlers.StrictFill;
import lombok.Data;

import java.util.Date;
import java.util.List;
@Data
public class TopicPreviewVo {
    int id;
    int type;
    String title;
    String text;
    List<String> images;
    Date time;
    Integer uid;
    String username;
    String avatar;
}
