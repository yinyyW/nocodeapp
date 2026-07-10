package com.ai.nocodeapp.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.nocodeapp.exception.ErrorCode;
import com.ai.nocodeapp.exception.ThrowUtils;
import com.ai.nocodeapp.manager.CosManager;
import com.ai.nocodeapp.service.ScreenshotService;
import com.ai.nocodeapp.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        // 1. 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR,
                "网页URL为空");
        // 2. 网页截图
        String localFilePath = WebScreenshotUtils.saveScreenshot(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(localFilePath),
                ErrorCode.PARAMS_ERROR, "网页URL为空");
        // 3. 上传截图
        try {
            String cosUrl = uploadScreenshot(localFilePath);
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.SYSTEM_ERROR, "上传截图失败");
            return cosUrl;
        } finally {
            // 4. 清理本地文件
            cleanLocalFile(localFilePath);
        }
    }

    /**
     * 上传截图至COS存储
     * @param localFilePath 本地保存路径
     * @return COS存储地址
     */
    private String uploadScreenshot(String localFilePath) {
        if (StrUtil.isBlank(localFilePath)) {
            return null;
        }
        File localFile = new File(localFilePath);
        if (!localFile.exists()) {
            log.error("截图文件不存在");
            return null;
        }
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        String key = generateScreenshotKey(fileName);
        return cosManager.uploadFile(key, localFile);
    }

    /**
     * 文件保存格式： /screenshots/2026/07/10/aaaabbbb_compressed.jpg
     * @param fileName 文件名
     * @return 保存至COS的key
     */
    private String generateScreenshotKey(String fileName) {
        String datePath =
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s", datePath, fileName);
    }

    /**
     * 清理本地截图
     * @param localFilePath
     */
    private void cleanLocalFile(String localFilePath) {
        File file = new File(localFilePath);
        if (file.exists()) {
            File parentDir = file.getParentFile();
            if (parentDir != null) {
                FileUtil.del(parentDir);
            }
        }
    }
}
