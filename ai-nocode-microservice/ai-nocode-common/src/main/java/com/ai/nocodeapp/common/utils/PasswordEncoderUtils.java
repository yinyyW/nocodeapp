package com.ai.nocodeapp.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类，使用 Spring Security BCrypt 对用户密码进行加盐加密。
 *
 * @author <a href="https://github.com/yinyyW">yinyyW</a>
 */
public class PasswordEncoderUtils {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 使用 BCrypt 算法对原始密码进行加盐加密。
     * 每次调用会生成不同的随机盐值，盐值随哈希结果一同返回。
     *
     * @param rawPassword 原始密码
     * @return 加盐哈希后的密文
     */
    public static String encryptPassword(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验原始密码与已加密的密码是否匹配。
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 已加密的密码密文
     * @return 匹配返回 true，否则 false
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}