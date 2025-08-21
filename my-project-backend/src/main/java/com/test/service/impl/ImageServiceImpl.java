package com.test.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.entity.dto.Account;
import com.test.entity.dto.StoreImage;
import com.test.mapper.AccountMapper;
import com.test.mapper.ImageStoreMapper;
import com.test.service.ImageService;
import com.test.utils.Const;
import com.test.utils.FlowUtils;
import io.minio.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/*
接收上传的文件和用户ID

生成唯一的文件名（UUID）

上传到MinIO的study存储桶的/avatar/目录

更新数据库中对应用户的头像字段
 */
@Slf4j
@Service
public class ImageServiceImpl extends ServiceImpl<ImageStoreMapper, StoreImage> implements ImageService{

    @Resource
    MinioClient client;
    @Resource
    AccountMapper accountMapper;

    @Resource
    FlowUtils flowUtils;

    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
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
            String avatar = accountMapper.selectById(id).getAvatar();
            this.deleteOldAvatar(avatar);
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

    /**
     * 上传图片到MinIO对象存储并保存图片信息到数据库
     * 包含频率限制功能，防止用户短时间内重复上传
     *
     * @param file 上传的图片文件
     * @param id 用户ID
     * @return 上传成功返回图片存储路径，失败返回null
     * @throws IOException 当文件读取出现IO异常时抛出
     */
    @Override
    public String uploadImage(MultipartFile file, int id) throws IOException {
        // 构建频率限制的Redis key：使用论坛图片计数器前缀 + 用户ID
        String key = Const.FORUM_IMAGE_COUNTER + id;

        // 频率限制检查：每3600秒（1小时）内最多允许上传20次
        // 如果超过限制，返回null拒绝上传
        if (!flowUtils.limitPeriodCounterCheck(key, 20, 3600))
            return null;

        // 生成唯一的文件名：使用UUID并移除连字符，确保文件名全局唯一
        String imageName = UUID.randomUUID().toString().replace("-","");

        // 获取当前时间，用于组织存储目录结构
        Date date = new Date();

        // 构建完整的对象存储路径：按日期分目录存储，便于管理和清理
        // 格式：/cache/yyyy-MM-dd/uuid
        imageName = "/cache/" + format.format(date) + "/" + imageName;

        // 构建MinIO上传参数
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket("study")                    // 指定存储桶名称
                .stream(file.getInputStream(),      // 文件输入流
                        file.getSize(),             // 文件大小（字节）
                        -1)                         // 分片大小，-1表示不分片（单文件上传）
                .object(imageName)                  // 对象存储路径
                .build();

        try{
            // 执行文件上传到MinIO对象存储
            client.putObject(args);

            // 创建图片存储实体对象，保存到数据库
            // StoreImage包含：用户ID、图片路径、上传时间等信息
            StoreImage storeImage = new StoreImage(id, imageName, date);

            // 保存图片信息到数据库，成功返回图片路径，失败返回null
            if (this.save(storeImage)){
                return imageName;                   // 上传成功，返回图片存储路径
            }else {
                // 数据库保存失败（可能是数据库连接问题或约束冲突）
                log.warn("图片信息数据库保存失败，用户ID: {}, 图片路径: {}", id, imageName);
                client.removeObject(RemoveObjectArgs.builder()
                        .bucket("study")
                        .object(imageName)
                        .build());
                return null;
            }

        }catch (Exception e) {
            // 记录异常日志，包括异常堆栈信息，便于问题排查
            log.error("图片上传出现问题：{}", e.getMessage(), e);
            return null;                            // 上传失败，返回null
        }
    }

    private void deleteOldAvatar(String avatar) throws Exception{
        if (avatar == null || avatar.isEmpty()) return;
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket("study")
                .object(avatar)
                .build();
        client.removeObject(removeObjectArgs);
    }
}
