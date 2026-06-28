package com.ai.nocodeapp.core.saver;

import cn.hutool.core.util.StrUtil;
import com.ai.nocodeapp.ai.model.MultiFileCodeResult;
import com.ai.nocodeapp.exception.BusinessException;
import com.ai.nocodeapp.exception.ErrorCode;
import com.ai.nocodeapp.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 多文件代码保存器
 */
public class MultiFileCodeSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {

    @Override
    protected String getBizType() {
        return CodeGenTypeEnum.MULTI_FILE.getValue();
    }

    @Override
    protected void validate(MultiFileCodeResult result) {
        super.validate(result);
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码不能为空");
        }
    }

    @Override
    protected void saveFiles(String dir, MultiFileCodeResult result) {
        writeToFile(dir, "index.html", result.getHtmlCode());
        writeToFile(dir, "style.css", result.getCssCode());
        writeToFile(dir, "script.js", result.getJsCode());
    }
}
