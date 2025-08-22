package com.test.utils;

public class Const {
    //jwt令牌
    public static final String JWT_BLACK_LIST = "jwt:blacklist:";


   //邮件验证
    public static final String VERIFY_EMAIL_LIMIT = "verify:email:limit";
    public static final String VERIFY_EMAIL_DATA = "verify:email:data";
    //过滤器优先级
    public static final int ORDER_LIMIT = -101 ;
    public static final int ORDER_CORS = -102;
    //请求频率限制
    public static final String FLOW_LIMIT_COUNTER = "flow:counter:";
    public static final String FLOW_LIMIT_BLOCK = "flow:block:";
    //论坛相关
    public static final String FORUM_WEATHER_CACHE = "weather:cache:";
    public static final String FORUM_IMAGE_COUNTER = "form:image:";
    public static final String FORUM_TOPIC_CREATE_COUNTER = "form:topic:create:";

    public static final String FORUM_TOPIC_PREVIEW_CACHE = "topic:preview:";

}
