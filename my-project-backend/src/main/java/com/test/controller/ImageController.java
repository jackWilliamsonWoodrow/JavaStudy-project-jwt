package com.test.controller;

import com.test.entity.RestBean;
import com.test.service.ImageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@Slf4j
@RestController
@RequestMapping("/api/image")
public class ImageController {
    @Resource
    ImageService imageService;
//上传头像，上传文件和根据用户id来上传头像的url
    @PostMapping("/avatar")
    public RestBean<String> uploadAvatar(@RequestParam("file")MultipartFile file,
                                         @RequestAttribute("id")int id) throws IOException {
        if (file.getSize() > 1024*1024* 5)
            return RestBean.failure(400,"头像图片不可以大于5MB");
        log.info("正在进行头像上传。。。");
        String url = imageService.uploadAvatar(file, id);
        if (url != null){
            log.info("头像上传成功，大小："+file.getSize());
            return RestBean.success(url);
        }else {
            return RestBean.failure(400,"头像上传失败，请联系管理员");
        }
    }
}
