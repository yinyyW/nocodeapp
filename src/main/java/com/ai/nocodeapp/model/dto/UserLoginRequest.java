package com.ai.nocodeapp.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 账户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
