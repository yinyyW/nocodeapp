package com.ai.nocodeapp.service;

public interface ScreenshotService {
    /**
     * 生成网页截图并上传至 COS
     * @param webUrl 网页url
     * @return COS 存储库地址
     */
    String generateAndUploadScreenshot(String webUrl);
}
