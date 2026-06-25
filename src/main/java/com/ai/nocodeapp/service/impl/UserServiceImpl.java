package com.ai.nocodeapp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.nocodeapp.common.utils.PasswordEncoderUtils;
import com.ai.nocodeapp.exception.BusinessException;
import com.ai.nocodeapp.exception.ErrorCode;
import com.ai.nocodeapp.model.dto.UserQueryRequest;
import com.ai.nocodeapp.model.enums.UserRoleEnum;
import com.ai.nocodeapp.model.vo.UserVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ai.nocodeapp.model.entity.User;
import com.ai.nocodeapp.mapper.UserMapper;
import com.ai.nocodeapp.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户 服务层实现。 *
 *
 * @author <a href="https://github.com/yinyyW">yinyyW</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public long userRegister(String userAccount, String userPassword,
                             String checkPassword) {
        // 1. 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");
        }
        if (userAccount.length() < 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户名称过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不一致");
        }

        // 2. 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long userCount = getMapper().selectCountByQuery(queryWrapper);
        if (userCount > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        // 3. 密码加密
        String encryptPassword =
                PasswordEncoderUtils.encryptPassword(userPassword);

        // 4. 创建用户并返回ID
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("用户名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户注册失败");
        }

        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword) {
        // 1.校验参数
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");
        }
        if (userAccount.length() < 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户名称过短");
        }
        // 2.获取用户
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        User user = getMapper().selectOneByQuery(queryWrapper);

        // 3.校验密码
        if (user == null || !PasswordEncoderUtils.matches(userPassword,
                user.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        return user;
    }

    @Override
    public User userInfo(long userId) {
        // 1. 数据库查询用户
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user;
    }

    @Override
    public UserVO convertToUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> convertToUserVOList(List<User> users) {
        if (CollUtil.isEmpty(users)) {
            return new ArrayList<>();
        }
        return users.stream()
                .map(this::convertToUserVO)
                .collect(Collectors.toList());
    }

    @Override
    public User addUser(User newUser) {
        // 1. 设置默认密码
        final String defaultPassword = "12345678";
        String encryptPassword =
                PasswordEncoderUtils.encryptPassword(defaultPassword);
        newUser.setUserPassword(encryptPassword);

        // 2. 如果前端未传入userAccount，自动生成
        if (newUser.getUserAccount() == null) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            newUser.setUserAccount("user_" + uuid.substring(0, 8));
        }

        // 3. 保存至数据库
        boolean result = this.save(newUser);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return newUser;
    }

    @Override
    public Page<User> queryUsers(UserQueryRequest userQueryRequest) {
        // 1. 创建QueryWrapper
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userRole = userQueryRequest.getUserRole();
        String userProfile = userQueryRequest.getUserProfile();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", id)
                .eq("userRole", userRole)
                .like("userProfile", userProfile)
                .like("userAccount", userAccount)
                .like("userName", userName)
                .orderBy(sortField, "ascend".equals(sortOrder));

        // 2. 分页查询
        int pageNum = userQueryRequest.getPageNum();
        int pageSize = userQueryRequest.getPageSize();
        return page(Page.of(pageNum, pageSize), queryWrapper);
    }

}
