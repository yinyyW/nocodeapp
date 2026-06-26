package com.ai.nocodeapp.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.nocodeapp.ai.model.HtmlCodeResult;
import com.ai.nocodeapp.ai.model.MultiFileCodeResult;
import com.ai.nocodeapp.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 保存ai生成的代码
 */
public class CodeFileSaver {
    /**
     * 获取文件保存的根目录
     */
    private static final String ROOT_DIR = System.getProperty("user.dir") +
            "/tmp/output";

    /**
     * @param bizType 生成的代码类型
     * 创建保存文件的路径: tmp/output/bizType_雪花id
     */
    private static String getSaveDir(String bizType) {
        String dirName = StrUtil.format("{}_{}", bizType,
                IdUtil.getSnowflakeNextIdStr());
        String dirPath = ROOT_DIR + File.separator + dirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 保存文件
     * @param dir 文件保存目录
     * @param fileName 文件名
     * @param code 需保存的代码
     */
    private static void writeToFile(String dir, String fileName, String code) {
        String path = dir + File.separator + fileName;
        FileUtil.writeString(code, path, StandardCharsets.UTF_8);
    }

    /**
     * 保存html代码
     * @param code html代码
     * @return 保存至本地文件的目录
     */
    public static File saveHtmlCode(HtmlCodeResult code) {
        String dir = getSaveDir(CodeGenTypeEnum.HTML.getValue());
        writeToFile(dir, "index.html", code.getHtmlCode());
        return new File(dir);
    }

    /**
     * 保存多文件代码
     * @param code html, css, js代码
     * @return 保存至本地文件的目录
     */
    public static File saveMultiFileCode(MultiFileCodeResult code) {
        String dir = getSaveDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        writeToFile(dir, "index.html", code.getHtmlCode());
        writeToFile(dir, "index.css", code.getCssCode());
        writeToFile(dir, "index.js", code.getJsCode());
        return new File(dir);
    }
}
