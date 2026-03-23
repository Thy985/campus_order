package com.xingchen.backend.util.constant;

/**
 * 错误码常量
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
    public static final int USER_EMAIL_EXIST = 20009;
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
    // 评价相关错误?(32000-32999)
    // ============================================
    public static final int REVIEW_EXIST = 32001;
    public static final int REVIEW_NOT_EXIST = 32002;

    // ============================================
    // 优惠券相关错误码 (33000-33999)
    // ============================================
    public static final int COUPON_NOT_EXIST = 33001;
    public static final int COUPON_EXPIRED = 33002;
    public static final int COUPON_ALREADY_CLAIMED = 33003;
    public static final int COUPON_NO_STOCK = 33004;
    public static final int COUPON_NOT_MEET_CONDITION = 33005;
    public static final int COUPON_ALREADY_USED = 33006;
    public static final int PRODUCT_CATEGORY_NOT_EXIST = 31004;
    
    // ============================================
    // 订单相关错误?(40000-40999)
    // ============================================
    public static final int ORDER_NOT_EXIST = 40001;
    public static final int ORDER_STATUS_ERROR = 40002;
    public static final int ORDER_CANCELLED = 40003;
    public static final int ORDER_CANNOT_CANCEL = 40004;
    public static final int ORDER_TIMEOUT = 40005;
    public static final int ORDER_AMOUNT_ERROR = 40006;
    public static final int ORDER_CREATE_FAILED = 40007;
    
    // ============================================
    // 支付相关错误?(50000-50999)
    // ============================================
    public static final int PAYMENT_FAILED = 50001;
    public static final int PAYMENT_TIMEOUT = 50002;
    public static final int PAYMENT_CHANNEL_ERROR = 50003;
    public static final int PAYMENT_AMOUNT_ERROR = 50004;
    public static final int PAYMENT_NOT_EXIST = 50005;
    public static final int PAYMENT_PROCESSING = 50006;
    public static final int ORDER_ALREADY_PAID = 50007;
    
    // ============================================
    // 文件相关错误?(60000-60999)
    // ============================================
    public static final int FILE_UPLOAD_FAILED = 60001;
    public static final int FILE_TYPE_ERROR = 60002;
    public static final int FILE_SIZE_EXCEED = 60003;
    public static final int FILE_NOT_EXIST = 60004;
    
    private ErrorCode() {
        // 私有构造函数，防止实例化
    }
}
