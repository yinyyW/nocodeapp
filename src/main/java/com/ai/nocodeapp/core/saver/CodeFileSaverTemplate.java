package com.ai.nocodeapp.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.nocodeapp.exception.BusinessException;
import com.ai.nocodeapp.exception.ErrorCode;
import com.ai.nocodeapp.exception.ThrowUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static com.ai.nocodeapp.constants.AppConstant.CODE_OUTPUT_ROOT_DIR;

/**
 * 文件代码保存器 - 模板模式
 */
public abstract class CodeFileSaverTemplate<T> {

    /**
     * 获取文件保存的根目录
     */
    private static final String ROOT_DIR = CODE_OUTPUT_ROOT_DIR;

    /**
     * @param appId 应用id
     * 创建保存文件的路径: tmp/output/bizType_雪花id
     */
    protected String getSaveDir(Long appId) {
        ThrowUtils.throwIf(appId == null || appId < 0, ErrorCode.PARAMS_ERROR);
        String dirName = StrUtil.format("{}_{}", getBizType(),
                appId);
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
    protected void writeToFile(String dir, String fileName, String code) {
        if (StrUtil.isBlank(code)) {
            return;
        }
        String path = dir + File.separator + fileName;
        FileUtil.writeString(code, path, StandardCharsets.UTF_8);
    }

    /**
     * @param result 校验参数
     */
    protected void validate(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码生成结果不能未空");
        }
    }

    /**
     * 保存代码
     * @param appId 应用id
     * @param result 代码
     * @return 保存至本地文件的目录
     */
    public final File saveCode(Long appId, T result) {
        // 1.校验参数
        validate(result);
        // 2.获取保存路径
        String saveDir = getSaveDir(appId);
        // 3.保存文件
        saveFiles(saveDir, result);
        return new File(saveDir);
    }

    /**
     * 获取保存的代码类型
     * @return 保存的代码类型
     */
    protected abstract String getBizType();

    /**
     * 保存代码文件
     * @param dir 保存目录
     * @param result 代码生成结果
     */
    protected abstract void saveFiles(String dir, T result);
}
