package com.ai.nocodeapp.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.ai.nocodeapp.annotation.AuthCheck;
import com.ai.nocodeapp.common.network.BaseResponse;
import com.ai.nocodeapp.common.utils.ResultUtils;
import com.ai.nocodeapp.constants.AppConstant;
import com.ai.nocodeapp.constants.UserConstant;
import com.ai.nocodeapp.exception.BusinessException;
import com.ai.nocodeapp.exception.ErrorCode;
import com.ai.nocodeapp.exception.ThrowUtils;
import com.ai.nocodeapp.model.dto.app.*;
import com.ai.nocodeapp.model.entity.App;
import com.ai.nocodeapp.model.entity.User;
import com.ai.nocodeapp.model.enums.CodeGenTypeEnum;
import com.ai.nocodeapp.model.vo.app.AppVO;
import com.ai.nocodeapp.service.AppService;
import com.ai.nocodeapp.service.ProjectDownloadService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.ai.nocodeapp.constants.UserConstant.USER_LOGIN_STATE;

/**
 * 应用 控制层。
 *
 * @author <a href="https://github.com/yinyyW">yinyyW</a>
 */
@Slf4j
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private ProjectDownloadService projectDownloadService;

    /**
     * 添加应用
     *
     * @param appAddRequest 添加应用请求
     * @param httpSession   http session
     * @return 应用id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest
            , HttpSession httpSession) {
        // 1. 校验参数
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 2. 获取身份信息
        Object userObj = httpSession.getAttribute(USER_LOGIN_STATE);
        User userInfo = (User) userObj;
        if (userInfo == null || userInfo.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 3. 添加应用
        Long appId = appService.createApp(appAddRequest, userInfo);
        return ResultUtils.success(appId);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody AppDeleteRequest appDeleteRequest, HttpSession httpSession) {
        // 1. 校验参数
        ThrowUtils.throwIf(appDeleteRequest == null || appDeleteRequest.getId() == null || appDeleteRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        // 2. 获取身份信息
        Object userObj = httpSession.getAttribute(USER_LOGIN_STATE);
        User userInfo = (User) userObj;
        if (userInfo == null || userInfo.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 3. 删除应用
        boolean deleteResult = appService.deleteApp(appDeleteRequest.getId(),
                userInfo);
        ThrowUtils.throwIf(!deleteResult, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    /**
     * 更新应用信息
     *
     * @param appUpdateRequest 更新应用请求
     * @param httpSession      http session
     * @return 更新应用结果
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpSession httpSession) {
        // 1. 校验参数
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null || appUpdateRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        // 2. 获取用户和应用信息
        Object userObj = httpSession.getAttribute(USER_LOGIN_STATE);
        User userInfo = (User) userObj;
        if (userInfo == null || userInfo.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        App oldApp = appService.getById(appUpdateRequest.getId());
        ThrowUtils.throwIf(oldApp == null || oldApp.getId() == null,
                ErrorCode.PARAMS_ERROR);
        // 3. 仅本人更新
        if (!oldApp.getUserId().equals(userInfo.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仅本人更新");
        }
        // 4. 更新应用
        App app = new App();
        BeanUtils.copyProperties(appUpdateRequest, app);
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getApp(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null || id < 0, ErrorCode.PARAMS_ERROR);
        AppVO appVO = appService.findAppVOById(id);
        ThrowUtils.throwIf(appVO == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(appVO);
    }

    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppVOPage(@RequestBody AppQueryRequest appQueryRequest, HttpSession httpSession) {
        // 1. 校验参数
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageNum <= 0 || pageSize > 20,
                ErrorCode.PARAMS_ERROR);
        // 2. 获取用户
        Object userObj = httpSession.getAttribute(USER_LOGIN_STATE);
        User userInfo = (User) userObj;
        if (userInfo == null || userInfo.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        appQueryRequest.setUserId(userInfo.getId());
        // 3. 查询当前用户的应用
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> pageApps = appService.page(Page.of(pageNum, pageSize),
                queryWrapper);
        // 4. 封装位AppVO类
        List<AppVO> appVOList = appService.getAppVOList(pageApps.getRecords());
        Page<AppVO> appVOPage = Page.of(pageNum, pageSize,
                pageApps.getTotalRow());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 分页获取精选应用列表
     *
     * @param appQueryRequest 查询请求
     * @return 精选应用列表
     */
    @PostMapping("/good/list/page/vo")
    public BaseResponse<Page<AppVO>> listGoodAppVOByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 限制每页最多 20 个
        int pageSize = appQueryRequest.getPageSize();
        int pageNum = appQueryRequest.getPageNum();
        ThrowUtils.throwIf(pageSize > 20 || pageNum < 0,
                ErrorCode.PARAMS_ERROR);
        // 只查询精选的应用
        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        // 分页查询
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize),
                queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize,
                appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 管理员删除应用
     *
     * @param appDeleteRequest 删除请求
     * @return 删除结果
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody AppDeleteRequest appDeleteRequest) {
        ThrowUtils.throwIf(appDeleteRequest == null || appDeleteRequest.getId() == null || appDeleteRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        // 判断是否存在
        Long id = appDeleteRequest.getId();
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = appService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 管理员更新应用
     *
     * @param appAdminUpdateRequest 更新请求
     * @return 更新结果
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null || appAdminUpdateRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        long id = appAdminUpdateRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        App app = new App();
        BeanUtils.copyProperties(appAdminUpdateRequest, app);
        // 设置编辑时间
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员分页获取应用列表
     *
     * @param appQueryRequest 查询请求
     * @return 应用列表
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppVOByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int pageSize = appQueryRequest.getPageSize();
        int pageNum = appQueryRequest.getPageNum();
        ThrowUtils.throwIf(pageSize > 20 || pageNum < 0,
                ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize),
                queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize,
                appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 管理员根据 id 获取应用详情
     *
     * @param id 应用 id
     * @return 应用详情
     */
    @GetMapping("/admin/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVO> getAppVOByIdByAdmin(Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        AppVO appVO = new AppVO();
        BeanUtils.copyProperties(app, appVO);
        return ResultUtils.success(appVO);
    }

    /**
     * 根据用户消息生成应用
     *
     * @param appId       应用id
     * @param userMessage 用户消息
     * @param httpSession httpSession
     * @return 流式输出
     */
    @GetMapping(value = "/chat/gen/code", produces =
            MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                                       @RequestParam String userMessage,
                                                       HttpSession httpSession) {
        // 1. 校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0,
                ErrorCode.PARAMS_ERROR, "应用id为空");
        ThrowUtils.throwIf(StrUtil.isBlank(userMessage),
                ErrorCode.PARAMS_ERROR, "用户消息为空");
        // 2. 获取登录用户
        Object userObj = httpSession.getAttribute(USER_LOGIN_STATE);
        User userInfo = (User) userObj;
        if (userInfo == null || userInfo.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 3. 生成代码
        return appService.chatToGenCode(appId, userMessage, userInfo)
                .map(chunk -> {
                    // 封装为json对象
                    Map<String, String> data = Map.of("d", chunk);
                    String jsonData = JSONUtil.toJsonStr(data);
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)
                            .build();
                })
                .concatWith(Mono.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data("")
                        .build()));
    }

    /**
     * 部署应用
     *
     * @param appDeployRequest 部署应用请求
     * @param session          http session
     * @return 部署的url
     */
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpSession session) {
        // 1. 校验参数
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        // 2. 部署应用
        User userInfo = (User) session.getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(userInfo == null, ErrorCode.NOT_LOGIN_ERROR);
        String url = appService.deployApp(appDeployRequest.getAppId(),
                userInfo);
        return ResultUtils.success(url);
    }

    /**
     * 取消部署应用
     *
     * @param appDeployRequest 部署应用请求
     * @param session          http session
     * @return 部署的url
     */
    @PostMapping("/deploy/cancel")
    public BaseResponse<Boolean> cancelDeployApp(@RequestBody AppDeployRequest appDeployRequest, HttpSession session) {
        // 1. 校验参数
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        // 2. 取消部署应用
        User userInfo = (User) session.getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(userInfo == null, ErrorCode.NOT_LOGIN_ERROR);
        appService.cancelDeploy(appDeployRequest.getAppId(), userInfo);
        return ResultUtils.success(true);
    }

    /**
     * 下载应用源码
     *
     * @param appId    应用ID
     * @param request  HTTP请求
     * @param response HTTP响应
     */
    @GetMapping("/download/{appId}")
    public void downloadAppCode(@PathVariable Long appId,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0,
                ErrorCode.PARAMS_ERROR, "应用ID为空");
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 2. 获取当前用户
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User userInfo = (User) userObj;
        ThrowUtils.throwIf(userInfo == null || userInfo.getId() == null,
                ErrorCode.NOT_LOGIN_ERROR);

        // 3. 权限校验
        ThrowUtils.throwIf(!app.getUserId().equals(userInfo.getId()),
                ErrorCode.NO_AUTH_ERROR, "无权下载该应用代码");

        // 4. 获取应用源码路径
        String projectDirName = app.getCodeGenType() + "_" + appId;
        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + projectDirName;
        File file = new File(projectPath);
        ThrowUtils.throwIf(!file.exists(), ErrorCode.NOT_FOUND_ERROR, "项目源码文件不存在");
        ThrowUtils.throwIf(!file.isDirectory(), ErrorCode.NOT_FOUND_ERROR, "非法项目源码文件路径");

        // 5. 下载源码
        String downloadName = appId + "_code";
        try {
            projectDownloadService.downloadProjectAsZip(projectPath, downloadName, response);
        } catch (Throwable e) {
            log.error("下载源码失败");
        }
    }

}
