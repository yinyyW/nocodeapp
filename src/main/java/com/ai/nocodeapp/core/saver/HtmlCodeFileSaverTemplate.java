package com.ai.nocodeapp.core.saver;

import cn.hutool.core.util.StrUtil;
import com.ai.nocodeapp.ai.model.HtmlCodeResult;
import com.ai.nocodeapp.exception.BusinessException;
import com.ai.nocodeapp.exception.ErrorCode;
import com.ai.nocodeapp.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * HTML代码保存器
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    @Override
    protected String getBizType() {
        return CodeGenTypeEnum.HTML.getValue();
    }

    @Override
    protected void validate(HtmlCodeResult result) {
        super.validate(result);
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码不能为空");
        }
    }

    @Override
    protected void saveFiles(String dir, HtmlCodeResult result) {
        writeToFile(dir, "index.html", result.getHtmlCode());
    }
}
