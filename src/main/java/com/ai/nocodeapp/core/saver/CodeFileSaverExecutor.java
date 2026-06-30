package com.ai.nocodeapp.core.saver;

import com.ai.nocodeapp.ai.model.HtmlCodeResult;
import com.ai.nocodeapp.ai.model.MultiFileCodeResult;
import com.ai.nocodeapp.exception.BusinessException;
import com.ai.nocodeapp.exception.ErrorCode;
import com.ai.nocodeapp.model.enums.CodeGenTypeEnum;

import java.io.File;

public class CodeFileSaverExecutor {

    /**
     * 单文件代码保存器
     */
    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaverTemplate =
                new HtmlCodeFileSaverTemplate();

    /**
     * 多文件代码保存器
     */
    private static final MultiFileCodeSaverTemplate multiFileCodeSaverTemplate =
                new MultiFileCodeSaverTemplate();


    /**
     * 根据代码生成类型保存代码
     * @param appId 应用id
     * @param codeGenTypeEnum 代码生成类型
     * @return 保存的目录
     */
    public static File saveCode(Long appId, CodeGenTypeEnum codeGenTypeEnum, Object result) {
        return switch (codeGenTypeEnum) {
            case CodeGenTypeEnum.HTML -> {
                yield htmlCodeFileSaverTemplate.saveCode(appId, (HtmlCodeResult) result);
            }
            case  CodeGenTypeEnum.MULTI_FILE -> {
                yield multiFileCodeSaverTemplate.saveCode(appId, (MultiFileCodeResult)  result);
            }
            default -> {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持对象类型");
            }
        };
    }
}
