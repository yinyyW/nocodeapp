package com.ai.nocodeapp.model.vo.user;

import lombok.Data;

@Data
public class UserRegisterResponse {
    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户id
     */
    private long userId;
}
