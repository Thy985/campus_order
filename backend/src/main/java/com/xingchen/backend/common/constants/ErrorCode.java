package com.xingchen.backend.common.constants;

/**
 * 错误码常量定义
 */
public class ErrorCode {
    
    // ============================================
    // 系统错误码(10000-10999)
    // ============================================
    public static final int SUCCESS = 200;
    public static final int SYSTEM_ERROR = 500;
    public static final int PARAM_ERROR = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    
    public static final int PARAM_VALIDATE_ERROR = 10001;
    public static final int DATA_NOT_EXIST = 10002;
    public static final int SYSTEM_BUSY = 10003;
    public static final int OPERATION_FAILED = 10004;
    
    // ============================================
    // 用户相关错误码(20000-20999)
    // ============================================
    public static final int USER_NOT_EXIST = 20001;
    public static final int USER_PASSWORD_ERROR = 20002;
    public static final int USER_PHONE_EXIST = 20003;
    public static final int USER_TOKEN_EXPIRED = 20004;
    public static final int USER_NO_PERMISSION = 20005;
    public static final int USER_DISABLED = 20006;
    public static final int USER_VERIFY_CODE_ERROR = 20007;
    public static final int USER_VERIFY_CODE_EXPIRED = 20008;
    
    // ============================================
    // 商家相关错误码(30000-30999)
    // ============================================
    public static final int MERCHANT_NOT_EXIST = 30001;
    public static final int MERCHANT_CLOSED = 30002;
    public static final int MERCHANT_NOT_OPEN = 30003;
    public static final int MERCHANT_NOT_MATCH = 30004;
    
    // ============================================
    // 商品相关错误码(31000-31999)
    // ============================================
    public static final int PRODUCT_NOT_EXIST = 31001;
    public static final int PRODUCT_OFF_SHELF = 31002;
    public static final int PRODUCT_STOCK_NOT_ENOUGH = 31003;

    // ============================================
    // 订相关错误码(32000-32999)
    // ============================================
    public static final int ORDER_NOT_EXIST = 32001;
    public static final int ORDER_STATUS_ERROR = 32002;
    public static final int ORDER_ALREADY_PAID = 32003;
    public static final int ORDER_EXPIRED = 32004;
    public static final int ORDER_NOT_PAID = 32005;
    public static final int ORDER_CAN_NOT_CANCEL = 32006;
    
    // ============================================
    // 支付相关错误码(33000-33999)
    // ============================================
    public static final int PAYMENT_FAILED = 33001;
    public static final int PAYMENT_AMOUNT_ERROR = 33002;
    public static final int PAYMENT_CHANNEL_ERROR = 33003;
    public static final int PAYMENT_SIGN_ERROR = 33004;
    public static final int PAYMENT_CALLBACK_ERROR = 33005;
    public static final int REFUND_FAILED = 33006;
    
    // ============================================
    // 文件相关错误码(34000-34999)
    // ============================================
    public static final int FILE_UPLOAD_FAILED = 34001;
    public static final int FILE_TYPE_NOT_SUPPORT = 34002;
    public static final int FILE_SIZE_EXCEED = 34003;
    public static final int FILE_NOT_EXIST = 34004;
    
    // ============================================
    // 短信相关错误码(35000-35999)
    // ============================================
    public static final int SMS_SEND_FAILED = 35001;
    public static final int SMS_TEMPLATE_NOT_EXIST = 35002;
    public static final int SMS_RATE_LIMIT = 35003;
}
