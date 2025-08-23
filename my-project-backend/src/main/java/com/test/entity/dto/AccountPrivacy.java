package com.test.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.test.entity.BaseData;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

@Data
@TableName("db_account_privacy")
public class AccountPrivacy implements BaseData{
    @TableId(type = IdType.AUTO)
    final Integer id;
    boolean phone = true;
    boolean email = true;
    boolean wx = true;
    boolean qq = true;
    boolean gender = true;


    //根据用户隐私设置，排除隐藏的id
    public String[] hiddenFields(){
        List<String> strings = new LinkedList<>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields){
            try {
                if (field.getType().equals(boolean.class) && !field.getBoolean(this)){
                    strings.add(field.getName());
                }
            }catch (Exception ignored){

            }
        }
        return strings.toArray(new String[0]);
    }
}
