package com.xingchen.backend.util.exception;

/**
 * @deprecated 已迁移至 {@link com.xingchen.backend.exception.BusinessException}，请更新 import 路径。
 * 此类仅为向后兼容保留，将在后续版本移除。
 */
@Deprecated
public class BusinessException extends com.xingchen.backend.exception.BusinessException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Integer code, String message) {
        super(code, message);
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
