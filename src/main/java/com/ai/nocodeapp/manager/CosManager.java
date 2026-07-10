package com.ai.nocodeapp.manager;

import com.ai.nocodeapp.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传文件至文件存储桶
     *
     * @param key COS存储路径
     * @param file 上传的文件
     * @return COS存储URL
     */
    public String uploadFile(String key, File file) {
        PutObjectRequest putObjectRequest =
                new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        try {
            PutObjectResult result = cosClient.putObject(putObjectRequest);
            if (result != null) {
                String url = cosClientConfig.getHost() + key;
                log.info("文件 {} 上传至 COS 成功", file.getName());
                return url;
            } else {
                log.info("文件 {} 上传至 COS 失败", file.getName());
                return null;
            }
        } catch (CosServiceException e) {
            log.error("文件上传至 COS 失败： {}", e.getMessage());
            return null;
        }
    }
}
