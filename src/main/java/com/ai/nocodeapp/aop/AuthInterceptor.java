package com.ai.nocodeapp.aop;

import com.ai.nocodeapp.annotation.AuthCheck;
import com.ai.nocodeapp.exception.BusinessException;
import com.ai.nocodeapp.exception.ErrorCode;
import com.ai.nocodeapp.model.entity.User;
import com.ai.nocodeapp.model.enums.UserRoleEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.ai.nocodeapp.constants.UserConstant.USER_LOGIN_STATE;

@Aspect
@Component
public class AuthInterceptor {

    /**
     * 权限校验拦截器
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doAuthInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 1. 获取方法所需的权限
        String mustRole = authCheck.mustRole();
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        if (mustRole.isEmpty() || mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        // 2. 获取用户角色
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        ServletRequestAttributes servletAttrs = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = servletAttrs.getRequest();
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        User user =  (User) session.getAttribute(USER_LOGIN_STATE);
        if (user == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        UserRoleEnum userEnum = UserRoleEnum.getEnumByValue(user.getUserRole());
        // 3. 判断当前用户是否可以使用方法
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }
}
