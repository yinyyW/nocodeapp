package com.ai.nocodeapp.service;

import com.ai.nocodeapp.model.dto.app.AppQueryRequest;
import com.ai.nocodeapp.model.entity.App;
import com.ai.nocodeapp.model.vo.app.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

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
}
