package com.ai.nocodeapp.service;

import com.ai.nocodeapp.model.dto.UserQueryRequest;
import com.ai.nocodeapp.model.entity.User;
import com.ai.nocodeapp.model.vo.UserVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/yinyyW">yinyyW</a>
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword,
                      String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户名
     * @param userPassword 用户密码
     * @return User
     */
    User userLogin(String userAccount, String userPassword);

    /**
     * 获取用户信息
     *
     * @param userId 用户id
     * @return User
     */
    User userInfo(long userId);

    /**
     * 用户信息脱敏
     *
     * @param user 数据库返回的用户信息
     * @return 脱敏后的User
     */
    UserVO convertToUserVO(User user);

    /**
     * 用户列表信息脱敏
     *
     * @param users 数据库返回的用户信息列表
     * @return 脱敏后的用户信息列表
     */
    List<UserVO> convertToUserVOList(List<User> users);

    /**
     * 管理员添加用户
     *
     * @param newUser 用户信息
     * @return 用户
     */
    User addUser(User newUser);

    /**
     * 管理员查询用户
     * @param userQueryRequest
     * @return
     */
    Page<User> queryUsers(UserQueryRequest userQueryRequest);
}
