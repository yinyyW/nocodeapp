package com.ai.nocodeapp.controller;

import com.ai.nocodeapp.annotation.AuthCheck;
import com.ai.nocodeapp.common.network.BaseResponse;
import com.ai.nocodeapp.common.utils.ResultUtils;
import com.ai.nocodeapp.exception.BusinessException;
import com.ai.nocodeapp.exception.ErrorCode;
import com.ai.nocodeapp.exception.ThrowUtils;
import com.ai.nocodeapp.model.dto.*;
import com.ai.nocodeapp.model.vo.UserLoginResponse;
import com.ai.nocodeapp.model.vo.UserRegisterResponse;
import com.ai.nocodeapp.model.vo.UserVO;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import com.ai.nocodeapp.model.entity.User;
import com.ai.nocodeapp.service.UserService;

import java.util.ArrayList;
import java.util.List;

import static com.ai.nocodeapp.constants.UserConstant.ADMIN_ROLE;
import static com.ai.nocodeapp.constants.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 控制层�? *
 * @author <a href="https://github.com/yinyyW">yinyyW</a>
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 用户id
     */
    @PostMapping("/register")
    public BaseResponse<UserRegisterResponse> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 1. 参数校验
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);

        // 2.保存用户
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long userId = userService.userRegister(userAccount, userPassword,
                checkPassword);

        // 3.返回用户
        UserRegisterResponse userRegisterResponse = new UserRegisterResponse();
        userRegisterResponse.setUserId(userId);
        userRegisterResponse.setUserAccount(userAccount);
        return ResultUtils.success(userRegisterResponse);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @param session          HttpSession
     * @return 脱敏后的用户信息
     */
    @PostMapping("/login")
    public BaseResponse<UserLoginResponse> login(@RequestBody UserLoginRequest userLoginRequest, HttpSession session) {
        // 1.校验参数
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);

        // 2.获取用户名和密码
        String userName = userLoginRequest.getUsername();
        String userPassword = userLoginRequest.getPassword();

        // 3.用户登录逻辑
        User user = userService.userLogin(userName, userPassword);

        // 4.更新session中的user数据
        session.setAttribute(USER_LOGIN_STATE, user);

        // 5.返回脱敏后的User对象
        UserLoginResponse userLoginResponse = new UserLoginResponse();
        BeanUtils.copyProperties(user, userLoginResponse);
        return ResultUtils.success(userLoginResponse);
    }

    /**
     * 用户信息查询
     *
     * @param session HttpSession
     * @return 脱敏后的用户信息
     */
    @GetMapping("/login/info")
    public BaseResponse<UserLoginResponse> loginInfo(HttpSession session) {
        // 1.获取用户id
        Object userObj = session.getAttribute(USER_LOGIN_STATE);
        User userInfo = (User) userObj;
        if (userInfo == null || userInfo.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 2.从数据库中获取用户信�?        userInfo = userService.userInfo(userInfo.getId());

        // 3.用户信息脱敏
        UserLoginResponse userLoginResponse = new UserLoginResponse();
        BeanUtils.copyProperties(userInfo, userLoginResponse);
        return ResultUtils.success(userLoginResponse);
    }

    /**
     * 用户登出
     *
     * @param session HttpSession
     * @return 操作成功结果
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpSession session) {
        // 1. 判断是否登录
        Object userObj = session.getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 2. 删除当前session中缓存的user
        session.removeAttribute(USER_LOGIN_STATE);
        return ResultUtils.success(true);
    }

    /**
     * 管理员添加用�?     *
     * @param userAddRequest
     * @return
     */
    @PostMapping("/admin/add")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<UserRegisterResponse> add(@RequestBody UserAddRequest userAddRequest) {
        // 1. 参数校验
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 2. 添加用户
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        user = userService.addUser(user);
        // 3. 返回用户id
        UserRegisterResponse userRegisterResponse = new UserRegisterResponse();
        userRegisterResponse.setUserId(user.getId());
        return ResultUtils.success(userRegisterResponse);
    }

    /**
     * 管理员根据用户id查询用户信息
     *
     * @param userId 用户ID
     * @return
     */
    @GetMapping("/admin/user/info")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<User> adminUserInfo(@RequestParam Long userId) {
        // 1. 参数校验
        ThrowUtils.throwIf(userId == null || userId < 0,
                ErrorCode.PARAMS_ERROR);

        // 2. 数据库中查询用户
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);

        return ResultUtils.success(user);
    }

    /**
     * 获取脱敏后的用户信息
     *
     * @param userId
     * @return
     */
    @GetMapping("/userVO/info")
    public BaseResponse<UserVO> userVOInfo(@RequestParam Long userId) {
        // 1. 参数校验
        ThrowUtils.throwIf(userId == null || userId < 0,
                ErrorCode.PARAMS_ERROR);

        // 2. 数据库中查询用户
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);

        // 3. 返回userVO
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }

    /**
     * 根据用户id删除用户
     *
     * @param userId
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Boolean> delete(@RequestParam Long userId) {
        if (userId == null || userId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(userId);
        return ResultUtils.success(result);
    }

    /**
     * 管理员更新用户信�?     *
     * @param userUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Boolean> update(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        return ResultUtils.success(result);
    }

    /**
     * 管理员查询用户列�?     *
     * @param userQueryRequest
     * @return
     */
    @PostMapping("/admin/list/userVO")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> userVOList(@RequestBody UserQueryRequest userQueryRequest) {
        // 1.校验参数
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);

        // 2.查询数据
        List<User> users = userService.queryUsers(userQueryRequest);

        // 3.数据脱敏
        Page<UserVO> userVOPage = new Page<>(userQueryRequest.getPageNum(),
                userQueryRequest.getPageSize(), users.size());
        List<UserVO> userVOList = userService.convertToUserVOList(users);
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

}
