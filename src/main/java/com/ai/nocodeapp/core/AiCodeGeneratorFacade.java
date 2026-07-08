package com.ai.nocodeapp.core;

import com.ai.nocodeapp.ai.AiCodeGeneratorService;
import com.ai.nocodeapp.ai.AiCodeGeneratorServiceFactory;
import com.ai.nocodeapp.ai.model.HtmlCodeResult;
import com.ai.nocodeapp.ai.model.MultiFileCodeResult;
import com.ai.nocodeapp.core.parser.CodeParserExecutor;
import com.ai.nocodeapp.core.saver.CodeFileSaverExecutor;
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
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param appId 应用id
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(Long appId, String userMessage,
                                    CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService =
                aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult =
                        aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.saveCode(appId, codeGenTypeEnum,
                        htmlCodeResult);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult =
                        aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.saveCode(appId, codeGenTypeEnum,
                        multiFileCodeResult);
            }
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
     * @param appId 应用id
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(Long appId,
                                                  String userMessage,
                                                  CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService =
                aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream =
                        aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(appId, codeStream, codeGenTypeEnum);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream =
                        aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(appId, codeStream, codeGenTypeEnum);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                        errorMessage);
            }
        };
    }

    /**
     * 生成的代码并保存（流式）
     *
     * @param appId 应用id
     * @param codeStream 流式输出
     * @return 流式输出
     */
    private Flux<String> processCodeStream(Long appId, Flux<String> codeStream,
                                           CodeGenTypeEnum codeGenTypeEnum) {
        StringBuilder stringBuilder = new StringBuilder();
        return codeStream
                .doOnNext(chunk -> {
                    stringBuilder.append(chunk);
                })
                .doOnComplete(() -> {
                    try {
                        // 获取代码
                        String completeMessage = stringBuilder.toString();
                        Object parseResult =
                                CodeParserExecutor.executeParser(codeGenTypeEnum, completeMessage);
                        // 保存至本地
                        File saveDir =
                                CodeFileSaverExecutor.saveCode(appId,
                                        codeGenTypeEnum, parseResult);
                        log.info("流式代码保存成功： {}", saveDir.getAbsolutePath());
                    } catch (BusinessException e) {
                        log.error(e.getMessage());
                    }
                });
    }
}
