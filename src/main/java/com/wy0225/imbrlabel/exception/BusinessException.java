package com.wy0225.imbrlabel.exception;

import com.wy0225.imbrlabel.constant.ErrorCode;

public class BusinessException extends RuntimeException{
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public BusinessException(String noAuthError, String invalidToken, ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    // Getter 方法
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
