package com.test.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.test.entity.dto.Account;
import com.test.mapper.AccountMapper;
import com.test.service.ImageService;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/*
接收上传的文件和用户ID

生成唯一的文件名（UUID）

上传到MinIO的study存储桶的/avatar/目录

更新数据库中对应用户的头像字段
 */
@Slf4j
public class ImageServiceImpl implements ImageService {

    @Resource
    MinioClient client;
    @Resource
    AccountMapper accountMapper;
    @Override
    public String uploadAvatar(MultipartFile file, int id) throws IOException {
        String imageName = UUID.randomUUID().toString().replace("-","");
        imageName = "/avatar/" + imageName;
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket("study")
                .stream(file.getInputStream(), file.getSize(), -1)
                .object(imageName)
                .build();
        try {
            client.putObject(args);
            if (accountMapper.update(null, Wrappers.<Account>update()
                    .eq("id",id).set("avatar",imageName))>0){
                return imageName;
            }else {
                return null;
            }

        }catch (Exception e){
            log.error("图片上传出现问题："+e.getMessage(),e);
            return null;
        }
    }

    @Override
    public void fetchImageFromMinio(OutputStream stream, String image) throws Exception {
        GetObjectArgs args = GetObjectArgs.builder()
                .bucket("study")
                .object(image)
                .build();
        GetObjectResponse response = client.getObject(args);
        IOUtils.copy(response,stream);
    }
}
