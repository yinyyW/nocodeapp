package com.ai.nocodeapp.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ProjectDownloadService {

    /**
     * 下载代码压缩包
     * @param projectPath 应用源码路径
     * @param downloadName 下载名称
     * @param response 返回响应
     * @throws Throwable 异常处理
     */
    void downloadProjectAsZip(String projectPath, String downloadName,
                              HttpServletResponse response) throws Throwable;
}
