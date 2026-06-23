package com.ai.nocodeapp.common.network;

import com.ai.nocodeapp.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用相应类
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private String message;

    T data;

    public BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(int code, T data) {
        this(code, "", data);
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage(), null);
    }
}
