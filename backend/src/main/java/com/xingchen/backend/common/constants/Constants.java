package com.xingchen.backend.common.constants;

/**
 * 系统常量定义
 */
public class Constants {

    /**
     * 删除标志
     */
    public static class DeleteFlag {
        public static final int NOT_DELETED = 0;  // 未删除
        public static final int DELETED = 1;      // 已删除
    }

    /**
     * 用户状态
     */
    public static class UserStatus {
        public static final int DISABLED = 0;     // 禁用
        public static final int ENABLED = 1;       // 启用
        public static final int NORMAL = 1;        // 正常（等同于启用）
    }

    /**
     * 用户类型
     */
    public static class UserType {
        public static final int NORMAL = 0;       // 普通用户
        public static final int MERCHANT = 1;     // 商家
        public static final int ADMIN = 2;        // 管理员
    }

    /**
     * Redis过期时间
     */
    public static class RedisExpire {
        public static final long VERIFY_CODE = 300;      // 验证码5分钟
        public static final long TOKEN = 86400;          // Token 24小时
    }

    /**
     * 库存标志
     */
    public static class Stock {
        public static final int UNLIMITED = -1;   // 无限库存
    }

    /**
     * 订单状态 - 完整状态机
     */
    public static class OrderStatus {
        public static final int WAIT_PAY = 1;         // 待支付
        public static final int WAIT_ACCEPT = 2;      // 待接单（已支付）
        public static final int MAKING = 3;           // 制作中
        public static final int WAIT_PICKUP = 4;      // 待取餐
        public static final int COMPLETED = 5;        // 已完成
        public static final int CANCELLED = 6;        // 已取消
        public static final int REFUSED = 7;          // 已拒绝（商家）
    }

    /**
     * 订单状态流转规则
     */
    public static class OrderStatusTransition {
        public static final int[] FROM_WAIT_PAY = {OrderStatus.WAIT_ACCEPT, OrderStatus.CANCELLED};
        public static final int[] FROM_WAIT_ACCEPT = {OrderStatus.MAKING, OrderStatus.REFUSED, OrderStatus.CANCELLED};
        public static final int[] FROM_MAKING = {OrderStatus.WAIT_PICKUP};
        public static final int[] FROM_WAIT_PICKUP = {OrderStatus.COMPLETED};
    }

    /**
     * 支付状态
     */
    public static class PayStatus {
        public static final int UNPAID = 0;         // 未支付
        public static final int PAID = 1;           // 已支付
        public static final int REFUNDED = 2;       // 已退款
        public static final int PARTIAL_REFUND = 3; // 部分退款
    }

    /**
     * 支付渠道
     */
    public static class PayChannel {
        public static final int ALIPAY = 1;         // 支付宝
        public static final int WECHAT = 2;         // 微信支付
        public static final int BALANCE = 3;        // 余额支付
    }

    /**
     * 商品状态
     */
    public static class ProductStatus {
        public static final int OFF_SHELF = 0;      // 下架
        public static final int ON_SHELF = 1;       // 上架
    }

    /**
     * 通知类型
     */
    public static class NotificationType {
        public static final int SYSTEM = 1;         // 系统通知
        public static final int ORDER = 2;          // 订单通知
        public static final int ACTIVITY = 3;       // 活动通知
    }

    /**
     * 文件类型
     */
    public static class FileType {
        public static final int IMAGE = 1;          // 图片
        public static final int DOCUMENT = 2;       // 文档
        public static final int VIDEO = 3;          // 视频
        public static final int OTHER = 4;          // 其他
    }

    /**
     * 优惠券状态
     */
    public static class CouponStatus {
        public static final int INACTIVE = 0;       // 未生效
        public static final int ACTIVE = 1;         // 生效中
        public static final int EXPIRED = 2;        // 已过期
        public static final int DISABLED = 3;       // 已禁用
    }

    /**
     * 优惠券类型
     */
    public static class CouponType {
        public static final int FULL_REDUCTION = 1; // 满减券
        public static final int DISCOUNT = 2;       // 折扣券
    }
}
