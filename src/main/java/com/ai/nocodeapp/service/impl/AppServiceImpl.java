package com.ai.nocodeapp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.nocodeapp.exception.BusinessException;
import com.ai.nocodeapp.exception.ErrorCode;
import com.ai.nocodeapp.exception.ThrowUtils;
import com.ai.nocodeapp.mapper.AppMapper;
import com.ai.nocodeapp.model.dto.app.AppQueryRequest;
import com.ai.nocodeapp.model.entity.App;
import com.ai.nocodeapp.model.entity.User;
import com.ai.nocodeapp.model.vo.UserVO;
import com.ai.nocodeapp.model.vo.app.AppVO;
import com.ai.nocodeapp.service.AppService;
import com.ai.nocodeapp.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

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

    private final UserService userService;

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

}
