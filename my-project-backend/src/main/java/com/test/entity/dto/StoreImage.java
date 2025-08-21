package com.test.entity.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@TableName("db_image_store")
@AllArgsConstructor
public class StoreImage {
    @TableId("uid")
    Integer uid;
    String name;
    Date time;
}
