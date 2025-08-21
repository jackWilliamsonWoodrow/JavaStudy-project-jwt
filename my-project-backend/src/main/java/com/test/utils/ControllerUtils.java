package com.test.utils;

import com.test.entity.RestBean;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;
@Component
public class ControllerUtils {
    //supplier函数式接口不接收任何参数
//@FunctionalInterface
//public interface Supplier<T> {
//    T get();
//}
//返回指定类型的结果
//函数式接口，可用 lambda 表达式实现
    public RestBean<Void> messageHandle(Supplier<String> action){
        String message = action.get();
        return message == null ? RestBean.success() : RestBean.failure(400,message);
    }
}
