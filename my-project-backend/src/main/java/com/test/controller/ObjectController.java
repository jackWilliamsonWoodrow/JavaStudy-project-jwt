package com.test.controller;

import com.test.entity.RestBean;
import com.test.service.ImageService;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
public class ObjectController {

    @Resource
    ImageService imageService;

    @GetMapping("/images/avatar/**")
    public void imageFetch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        this.fetchImage(request, response);
    }
/*
 方法功能
从HTTP请求路径中提取图片路径

从MinIO获取图片并输出到HTTP响应流

处理404等异常情况

设置缓存控制头
Cache-Control 基本概念
1. 作用
控制缓存的行为：哪些内容可以缓存、缓存多长时间、如何验证缓存等

减少服务器负载，提高网站性能
// 缓存30天（2592000秒）
response.setHeader("Cache-Control", "max-age=2592000");
优化用户体验（加载更快）
对. 静态资源（图片、CSS、JS）设置长期缓存
 */
    private void fetchImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String imagePath = request.getServletPath().substring(7);
        ServletOutputStream stream = response.getOutputStream();
        if (imagePath.length() <= 13){
            response.setStatus(404);
            stream.println(RestBean.failure(404,"Not found").toString());
        }else {
            try {
                imageService.fetchImageFromMinio(stream,imagePath);
                response.setHeader("Cache-Control","max-age=2592000");
            } catch (ErrorResponseException e) {
                if (e.response().code() == 404){
                    response.setStatus(404);
                    stream.println(RestBean.failure(404,"Not found").toString());
                }else {
                    log.error("从Minio中获取图片出现异常："+e.getMessage(),e);
                }
            }
        }
    }
}
