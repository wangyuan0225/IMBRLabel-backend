package com.wy0225.imbrlabel.constant;

public enum ErrorCode {
    NO_AUTH_ERROR("NO_AUTH_ERROR", "Authentication failed due to invalid token");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
