package com.ai.nocodeapp.service;

import com.ai.nocodeapp.model.dto.app.AppQueryRequest;
import com.ai.nocodeapp.model.entity.App;
import com.ai.nocodeapp.model.entity.User;
import com.ai.nocodeapp.model.vo.app.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/yinyyW">yinyyW</a>
 */
public interface AppService extends IService<App> {

    /**
     * 获取AppVO对象
     *
     * @param id 应用id
     * @return AppVO
     */
    AppVO findAppVOById(Long id);

    /**
     * 获取分页查询queryWrapper
     *
     * @param appQueryRequest 分页查询请求
     * @return 分页查询QueryWrapper
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 关联appUser 信息
     *
     * @param apps 应用列表
     * @return AppVO列表
     */
    List<AppVO> getAppVOList(List<App> apps);

    /**
     * 删除应用
     *
     * @param id   应用id
     * @param user 当前操作用户
     * @return 删除应用结果
     */
    Boolean deleteApp(Long id, User user);

    /**
     * 根据用户消息生成应用
     * @param appId 应用id
     * @param userMessage 用户消息
     * @param user 用户信息
     * @return 流式输出
     */
    Flux<String> chatToGenCode(Long appId, String userMessage, User user);

    /**
     * 部署应用
     * @param appId 应用id
     * @param user 用户信息
     * @return 部署的url
     */
    String deployApp(Long appId, User user);

    /**
     * 取消部署的应用
     * @param appId 应用id
     * @param user 用户信息
     * @return 取消部署结果
     */
    Boolean cancelDeploy(Long appId, User user);
}
