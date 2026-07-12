package com.ai.nocodeapp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.nocodeapp.ai.AiCodeGenTypeRoutingService;
import com.ai.nocodeapp.constants.AppConstant;
import com.ai.nocodeapp.core.AiCodeGeneratorFacade;
import com.ai.nocodeapp.core.builder.VueProjectBuilder;
import com.ai.nocodeapp.core.handler.StreamHandlerExecutor;
import com.ai.nocodeapp.exception.BusinessException;
import com.ai.nocodeapp.exception.ErrorCode;
import com.ai.nocodeapp.exception.ThrowUtils;
import com.ai.nocodeapp.mapper.AppMapper;
import com.ai.nocodeapp.model.dto.app.AppAddRequest;
import com.ai.nocodeapp.model.dto.app.AppQueryRequest;
import com.ai.nocodeapp.model.entity.App;
import com.ai.nocodeapp.model.entity.User;
import com.ai.nocodeapp.model.enums.ChatHistoryMessageTypeEnum;
import com.ai.nocodeapp.model.enums.CodeGenTypeEnum;
import com.ai.nocodeapp.model.enums.UserRoleEnum;
import com.ai.nocodeapp.model.vo.app.AppVO;
import com.ai.nocodeapp.model.vo.user.UserVO;
import com.ai.nocodeapp.service.AppService;
import com.ai.nocodeapp.service.ChatHistoryService;
import com.ai.nocodeapp.service.ScreenshotService;
import com.ai.nocodeapp.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.compiler.CodeGen;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/yinyyW">yinyyW</a>
 */
@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private final UserService userService;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService;

    public AppServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public AppVO findAppVOById(Long id) {
        // 1. 校验参数
        ThrowUtils.throwIf(id == null || id < 0, ErrorCode.PARAMS_ERROR);
        // 2. 获取应用信息
        App app = getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        // 3. 获取用户信息
        User user = userService.getById(app.getUserId());
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        // 4. 封装AppVO类
        AppVO appVO = new AppVO();
        BeanUtils.copyProperties(app, appVO);
        appVO.setUser(userVO);
        return appVO;
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id, id != null)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority, priority != null)
                .eq("userId", userId, userId != null)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public List<AppVO> getAppVOList(List<App> apps) {
        // 1. 参数校验
        if (CollUtil.isEmpty(apps)) {
            return new ArrayList<>();
        }

        // 2. 关联用户列表
        Set<Long> userIds =
                apps.stream().map(App::getUserId).collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService
                .listByIds(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    return userVO;
                }));

        // 3. 返回AppVO
        return apps.stream().map(app -> {
            AppVO appVO = new AppVO();
            BeanUtils.copyProperties(app, appVO);
            appVO.setUser(userVOMap.get(app.getUserId()));
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Boolean deleteApp(Long id, User user) {
        // 1. 校验参数
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR);

        // 2. 获取应用信息
        App app = getById(id);
        ThrowUtils.throwIf(app == null || app.getId() == null,
                ErrorCode.PARAMS_ERROR);
        String deployKey = app.getDeployKey();
        String codeGenType = app.getCodeGenType();
        ThrowUtils.throwIf(codeGenType == null, ErrorCode.PARAMS_ERROR,
                "生成应用类型为空");

        // 3. 仅本人或管理员可删除
        if (!app.getUserId().equals(user.getId())
                && !user.getUserRole().equals(UserRoleEnum.ADMIN.getValue())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 4. 删除应用
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR);

        // 5. 删除应用部署文件
        if (!StrUtil.isBlank(deployKey)) {
            try {
                String deployPath =
                        AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
                File deployDir = new File(deployPath);
                if (deployDir.exists() && deployDir.isDirectory()) {
                    FileUtil.del(deployDir);
                }
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }

        // 6. 删除应用预览文件
        try {
            String previewPath =
                    AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + codeGenType + "_" + id;
            File previewDir = new File(previewPath);
            if (previewDir.exists() && previewDir.isDirectory()) {
                FileUtil.del(previewDir);
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return true;
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String userMessage,
                                      User user) {
        // 1. 校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0,
                ErrorCode.PARAMS_ERROR, "应用id为空");
        ThrowUtils.throwIf(StrUtil.isBlank(userMessage),
                ErrorCode.PARAMS_ERROR, "用户消息为空");
        // 2. 获取应用信息
        App app = getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        // 3. 仅本人可生成代码
        if (!user.getId().equals(app.getUserId())) {
            ThrowUtils.throwIf(StrUtil.isBlank(userMessage),
                    ErrorCode.NO_AUTH_ERROR, "仅本人可生成代码");
        }
        // 4. 获取生成类型
        CodeGenTypeEnum codeGenTypeEnum =
                CodeGenTypeEnum.getEnumByValue(app.getCodeGenType());
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR,
                "生成类型为空");
        // 5. 添加用户消息
        chatHistoryService.addChatMessage(appId, userMessage,
                ChatHistoryMessageTypeEnum.USER.getValue(),
                user.getId());
        // 6. 生成流式输出，添加AI回复消息
        Flux<String> codeStream =
                aiCodeGeneratorFacade.generateAndSaveCodeStream(appId,
                        userMessage, codeGenTypeEnum);
        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService
                , appId, user, codeGenTypeEnum);
    }

    @Override
    public String deployApp(Long appId, User user) {
        // 1. 校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(user == null || user.getId() == null,
                ErrorCode.NO_AUTH_ERROR);

        // 2. 获取应用信息
        App app = getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);

        // 3. 校验用户权限
        ThrowUtils.throwIf(!user.getId().equals(app.getUserId()),
                ErrorCode.NO_AUTH_ERROR, "仅可部署自己的应用");

        // 4. 生成deployKey
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }

        // 5. 移动代码至部署路径
        try {
            String codeGenType = app.getCodeGenType();
            ThrowUtils.throwIf(codeGenType == null, ErrorCode.PARAMS_ERROR,
                    "应用生成类型为空");
            String sourcePath =
                    AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + codeGenType + "_" + appId;
            String destPath =
                    AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
            File sourceDir = new File(sourcePath);
            ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(), ErrorCode.PARAMS_ERROR, "应用代码不存在");
            // 处理Vue工程项目文件
            if (CodeGenTypeEnum.VUE_PROJECT.getValue().equals(codeGenType)) {
                boolean buildResult =
                        vueProjectBuilder.buildProject(sourcePath);
                ThrowUtils.throwIf(!buildResult, ErrorCode.SYSTEM_ERROR, "Vue" +
                        "项目构建失败");
                File distDir = new File(sourceDir, "dist");
                ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR,
                        "dist目录不存在");
                sourceDir = distDir;
            }
            File destDir = new File(destPath);
            FileUtil.copyContent(sourceDir, destDir, true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "移动至部署路径失败");
        }
        // 6. 更新应用
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用失败");
        // 7. 生成截图封面并保存至COS存储库
        String appUrl = AppConstant.CODE_DEPLOY_HOST + "/" + deployKey;
        generateAndUploadScreenshotAsync(appUrl, appId);
        // 8. 返回部署url
        return appUrl;
    }

    @Override
    public void generateAndUploadScreenshotAsync(String webUrl, Long appId) {
        Thread.startVirtualThread(() -> {
            // 上传截图
            String coverUrl =
                    screenshotService.generateAndUploadScreenshot(webUrl);
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(coverUrl);
            boolean updateResult = updateById(updateApp);
            ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR,
                    "更新封面字段失败");
        });
    }

    @Override
    public Boolean cancelDeploy(Long appId, User user) {
        // 1. 校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        App app = getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        String deployKey = app.getDeployKey();
        String codeGenType = app.getCodeGenType();
        ThrowUtils.throwIf(deployKey == null, ErrorCode.PARAMS_ERROR, "应用未部署");
        ThrowUtils.throwIf(codeGenType == null, ErrorCode.PARAMS_ERROR,
                "应用生成类型为空");
        ThrowUtils.throwIf(!user.getId().equals(app.getUserId()),
                ErrorCode.NO_AUTH_ERROR);

        // 2. 更新数据库
        boolean updateResult = UpdateChain.of(App.class)
                .set(App::getDeployKey, null)
                .set(App::getDeployedTime, null)
                .where(App::getId)
                .eq(appId)
                .update();

        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "数据库更新异常");

        // 3. 删除部署文件
        try {
            String deployPath =
                    AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
            File deployDir = new File(deployPath);
            ThrowUtils.throwIf(!deployDir.exists() || !deployDir.isDirectory(), ErrorCode.PARAMS_ERROR);
            boolean delResult = FileUtil.del(deployPath);
            if (!delResult) {
                log.error("清理部署文件异常");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return true;
    }

    @Override
    public boolean removeById(@NonNull Serializable id) {
        // 1. 参数校验
        long appId = Long.parseLong(id.toString());
        ThrowUtils.throwIf(appId <= 0, ErrorCode.PARAMS_ERROR);

        // 2. 删除应用关联消息
        try {
            boolean deleteResult =
                    chatHistoryService.deleteMessageByAppId(appId);
            if (!deleteResult) {
                log.error("删除应用 {} 关联消息失败", appId);
            }
        } catch (Exception error) {
            log.error(error.getMessage());
        }

        // 3. 删除应用
        return super.removeById(id);
    }

    @Override
    public Long createApp(AppAddRequest appAddRequest
            , User user) {
        // 1. 校验参数
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(appAddRequest.getInitPrompt()),
                ErrorCode.PARAMS_ERROR, "初始化提示词为空");
        ThrowUtils.throwIf(user == null || user.getId() == null,
                ErrorCode.NOT_LOGIN_ERROR);

        // 2. 智能获取代码生成类型
        CodeGenTypeEnum codeGenTypeEnum =
                aiCodeGenTypeRoutingService.routeCodeGenType(appAddRequest.getInitPrompt()).getCodeGenerationType();
        if (codeGenTypeEnum == null) {
            codeGenTypeEnum = CodeGenTypeEnum.HTML;
        }
        // 3. 添加应用
        App app = new App();
        BeanUtils.copyProperties(appAddRequest, app);
        app.setUserId(user.getId());
        app.setAppName(app.getInitPrompt().substring(0,
                Math.min(12, app.getInitPrompt().length())));
        app.setCodeGenType(codeGenTypeEnum.getValue());
        boolean addResult = save(app);
        ThrowUtils.throwIf(!addResult, ErrorCode.OPERATION_ERROR, "添加应用失败");
        return app.getId();
    }
}
