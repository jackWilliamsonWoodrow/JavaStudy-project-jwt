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
import org.springframework.stereotype.Service;
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
@Service
public class ImageServiceImpl implements ImageService {

    @Resource
    MinioClient client;
    @Resource
    AccountMapper accountMapper;
    /**
     * 上传用户头像到MinIO对象存储并更新数据库记录
     *
     * @param file 上传的头像文件
     * @param id 用户ID
     * @return 上传成功返回图片存储路径，失败返回null
     * @throws IOException 当文件读取出现IO异常时抛出
     */
    @Override
    public String uploadAvatar(MultipartFile file, int id) throws IOException {
        // 生成唯一的文件名：使用UUID并移除连字符，确保文件名唯一性
        String imageName = UUID.randomUUID().toString().replace("-","");

        // 构建完整的对象存储路径：存放在avatar目录下
        imageName = "/avatar/" + imageName;

        // 构建MinIO上传参数
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket("study")                    // 指定存储桶名称
                .stream(file.getInputStream(),      // 文件输入流
                        file.getSize(),             // 文件大小
                        -1)                         // 分片大小，-1表示不分片
                .object(imageName)                  // 对象存储路径
                .build();

        try {
            // 执行文件上传到MinIO
            client.putObject(args);

            // 更新数据库中对应用户的头像字段
            // 使用MyBatis Plus的UpdateWrapper构建更新条件
            int updateCount = accountMapper.update(
                    null,
                    Wrappers.<Account>update()
                            .eq("id", id)                  // 条件：用户ID匹配
                            .set("avatar", imageName)       // 设置：更新头像路径字段
            );

            // 判断数据库更新是否成功（影响行数大于0表示成功）
            if (updateCount > 0){
                return imageName;                   // 返回图片存储路径
            } else {
                // 数据库更新失败（可能是用户不存在）
                log.warn("用户头像数据库更新失败，用户ID: {}", id);
                return null;
            }

        } catch (Exception e) {
            // 记录异常日志，包括异常堆栈信息
            log.error("图片上传出现问题：{}", e.getMessage(), e);
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
