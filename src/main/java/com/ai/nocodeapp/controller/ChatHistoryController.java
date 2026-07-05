package com.ai.nocodeapp.controller;

import com.ai.nocodeapp.annotation.AuthCheck;
import com.ai.nocodeapp.common.network.BaseResponse;
import com.ai.nocodeapp.common.utils.ResultUtils;
import com.ai.nocodeapp.constants.UserConstant;
import com.ai.nocodeapp.exception.BusinessException;
import com.ai.nocodeapp.exception.ErrorCode;
import com.ai.nocodeapp.exception.ThrowUtils;
import com.ai.nocodeapp.model.dto.chat.ChatHistoryQueryRequest;
import com.ai.nocodeapp.model.entity.ChatHistory;
import com.ai.nocodeapp.model.entity.User;
import com.ai.nocodeapp.service.ChatHistoryService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.ai.nocodeapp.constants.UserConstant.USER_LOGIN_STATE;

/**
 * 对话历史 控制层。
 *
 * @author <a href="https://github.com/yinyyW">yinyyW</a>
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                              HttpServletRequest request) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0,
                ErrorCode.PARAMS_ERROR, "应用ID不存在");

        // 2. 游标分页查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User userInfo = (User) userObj;
        if (userInfo == null || userInfo.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Page<ChatHistory> chatHistoryPage =
                chatHistoryService.listAppChatHistoryByPage(appId, pageSize,
                        lastCreateTime, userInfo);
        return ResultUtils.success(chatHistoryPage);
    }

    /**
     * 管理员分页查询所有对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        // 查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(result);
    }

}
