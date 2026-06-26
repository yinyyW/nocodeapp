package com.ai.nocodeapp.core;

import com.ai.nocodeapp.ai.AiCodeGeneratorService;
import com.ai.nocodeapp.ai.model.HtmlCodeResult;
import com.ai.nocodeapp.ai.model.MultiFileCodeResult;
import com.ai.nocodeapp.exception.BusinessException;
import com.ai.nocodeapp.exception.ErrorCode;
import com.ai.nocodeapp.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage,
                                    CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                        errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage,
                                                  CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCodeStream(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                        errorMessage);
            }
        };
    }

    /**
     * 生成 HTML 模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult result =
                aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCode(result);
    }

    /**
     * 生成多文件模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult result =
                aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCode(result);
    }

    /**
     * 生成 HTML 模式的代码并保存（流式）
     *
     * @param userMessage 用户提示词
     * @return 流式数据
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        StringBuilder stringBuilder = new StringBuilder();
        Flux<String> result =
                aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
        return result
                .doOnNext(chunk -> {
                    stringBuilder.append(chunk);
                })
                .doOnComplete(() -> {
                    try {
                        // 获取html代码
                        String completeMessage = stringBuilder.toString();
                        HtmlCodeResult htmlCodeResult =
                                CodeParser.parseHtmlCode(completeMessage);

                        // 保存至本地
                        File saveDir =
                                CodeFileSaver.saveHtmlCode(htmlCodeResult);
                        log.info("流式代码保存成功： {}", saveDir.getAbsolutePath());
                    } catch (BusinessException e) {
                        log.error(e.getMessage());
                    }
                });
    }

    /**
     * 生成多文件模式的代码并保存（流式）
     *
     * @param userMessage 用户提示词
     * @return 流式数据
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
        StringBuilder stringBuilder = new StringBuilder();

        return aiCodeGeneratorService.generateMultiFileCodeStream(userMessage)
                .doOnNext(stringBuilder::append)
                .doOnComplete(() -> {
                    try {
                        // 获取html代码
                        String completeMessage = stringBuilder.toString();
                        MultiFileCodeResult multiFileCodeResult =
                                CodeParser.parseMultiFileCode(completeMessage);

                        // 保存至本地
                        File saveDir =
                                CodeFileSaver.saveMultiFileCode(multiFileCodeResult);
                        log.info("流式代码保存成功： {}", saveDir.getAbsolutePath());
                    } catch (BusinessException e) {
                        log.error(e.getMessage());
                    }
                });
    }
}
