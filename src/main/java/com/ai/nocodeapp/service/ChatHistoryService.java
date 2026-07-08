package com.ai.nocodeapp.service;

import com.ai.nocodeapp.model.dto.chat.ChatHistoryQueryRequest;
import com.ai.nocodeapp.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.ai.nocodeapp.model.entity.ChatHistory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/yinyyW">yinyyW</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加消息记录
     * @param appId 应用id
     * @param message 消息
     * @param messageType 消息类型
     * @param userId 用户id
     * @return 添加消息结果
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 删除关联应用的消息
     * @param appId 应用id
     * @return 删除结果
     */
    boolean deleteMessageByAppId(Long appId);

    /**
     * 获取消息游标查询包装类
     * @param chatHistoryQueryRequest 消息游标查询请求
     * @return 消息游标查询包装类
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 聊天消息游标查询
     * @param appId 应用id
     * @param pageSize 分页大小
     * @param lastCreateTime 按创建时间倒叙查询
     * @param loginUser 用户信息
     * @return 分页游标查询聊天消息
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    /**
     * 加载应用会话消息至AI服务实例
     * @param appId 应用id
     * @param messageWindowChatMemory 会话记忆
     * @param maxCount 最大加载数量
     * @return 成功加载的消息数量
     */
    int loadChatHistoryToMemory(Long appId,
                                MessageWindowChatMemory messageWindowChatMemory, int maxCount);
}
