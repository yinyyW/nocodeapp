package com.ai.nocodeapp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.nocodeapp.constants.AppConstant;
import com.ai.nocodeapp.core.AiCodeGeneratorFacade;
import com.ai.nocodeapp.exception.BusinessException;
import com.ai.nocodeapp.exception.ErrorCode;
import com.ai.nocodeapp.exception.ThrowUtils;
import com.ai.nocodeapp.mapper.AppMapper;
import com.ai.nocodeapp.model.dto.app.AppQueryRequest;
import com.ai.nocodeapp.model.entity.App;
import com.ai.nocodeapp.model.entity.User;
import com.ai.nocodeapp.model.enums.CodeGenTypeEnum;
import com.ai.nocodeapp.model.vo.app.AppVO;
import com.ai.nocodeapp.model.vo.user.UserVO;
import com.ai.nocodeapp.service.AppService;
import com.ai.nocodeapp.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
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
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private final UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

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
        // 5. 生成流式输出
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(appId,
                userMessage, codeGenTypeEnum);
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
            File destDir = new File(destPath);
            FileUtil.copyContent(sourceDir, destDir, true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "移动至部署路径失败");
        }
        // 6. 更新应用
        App updateApp = new App();
        BeanUtils.copyProperties(app, updateApp);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用失败");

        // 7. 返回部署url
        return AppConstant.CODE_DEPLOY_HOST + "/" + deployKey;
    }

}
