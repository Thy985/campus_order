-- ============================================
-- 校园点餐管理系统 - 完整数据库初始化脚本 (整合优化版)
-- 版本：v8.0.0 (整合V5+V6+V7)
-- 日期：2026-03-22
-- 说明：
--   1. 整合V5、V6、V7所有变更
--   2. 优化表结构和索引
--   3. 统一字段命名规范
--   4. 添加幂等性和乐观锁支持
-- ============================================

USE `campus_order`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 第一部分：基础表结构
-- ============================================

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `gender` TINYINT UNSIGNED DEFAULT 0 COMMENT '性别:0-未知,1-男,2-女',
    `user_type` TINYINT UNSIGNED DEFAULT 0 COMMENT '用户类型:0-普通用户,1-商家,2-管理员',
    `merchant_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联的商家ID',
    `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态:0-禁用,1-正常',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `login_count` INT UNSIGNED DEFAULT 0 COMMENT '登录次数',
    `register_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `register_ip` VARCHAR(50) DEFAULT NULL COMMENT '注册IP',
    `password_modify_time` DATETIME DEFAULT NULL COMMENT '密码修改时间',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除:0-否,1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_user_type_status` (`user_type`, `status`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_last_login_time` (`last_login_time`),
    KEY `idx_register_time` (`register_time`),
    KEY `idx_status_is_deleted` (`status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商家分类表
DROP TABLE IF EXISTS `merchant_category`;
CREATE TABLE `merchant_category` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    `sort_order` INT UNSIGNED DEFAULT 0 COMMENT '排序',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除:0-否,1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家分类表';

-- 商家表
DROP TABLE IF EXISTS `merchant`;
CREATE TABLE `merchant` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商家ID',
    `name` VARCHAR(100) NOT NULL COMMENT '商家名称',
    `logo` VARCHAR(255) DEFAULT NULL COMMENT '商家Logo',
    `banner` VARCHAR(500) DEFAULT NULL COMMENT '商家横幅',
    `category_id` INT UNSIGNED NOT NULL COMMENT '商家分类ID',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '商家简介',
    `notice` VARCHAR(255) DEFAULT NULL COMMENT '商家公告',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `address` VARCHAR(255) DEFAULT '' COMMENT '商家地址',
    `min_order_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '最小订单金额',
    `avg_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '人均消费',
    `rating` DECIMAL(3,2) NOT NULL DEFAULT 5.00 COMMENT '评分(0-5)',
    `sales_volume` INT UNSIGNED DEFAULT 0 COMMENT '月销量',
    `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态:0-休息中,1-营业中,2-已关闭',
    `sort_order` INT UNSIGNED DEFAULT 0 COMMENT '排序',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除:0-否,1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id_status` (`category_id`, `status`),
    KEY `idx_status_sort_order` (`status`, `sort_order`),
    KEY `idx_sales_volume` (`sales_volume`),
    KEY `idx_rating` (`rating`),
    KEY `idx_is_deleted` (`is_deleted`),
    CONSTRAINT `fk_merchant_category` FOREIGN KEY (`category_id`) REFERENCES `merchant_category` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';

-- 商家营业时间表
DROP TABLE IF EXISTS `merchant_business_hours`;
CREATE TABLE `merchant_business_hours` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
    `day_of_week` TINYINT UNSIGNED NOT NULL COMMENT '星期:0-6(周日-周六)',
    `meal_type` TINYINT UNSIGNED NOT NULL COMMENT '餐次:1-早餐,2-午餐,3-晚餐',
    `start_time` TIME NOT NULL COMMENT '开始时间',
    `end_time` TIME NOT NULL COMMENT '结束时间',
    `is_open` TINYINT UNSIGNED DEFAULT 1 COMMENT '是否营业:0-否,1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_merchant_day_meal` (`merchant_id`, `day_of_week`, `meal_type`),
    KEY `idx_merchant_id` (`merchant_id`),
    CONSTRAINT `fk_business_hours_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家营业时间';

-- 商品分类表
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父分类ID',
    `level` INT UNSIGNED DEFAULT 1 COMMENT '分类层级',
    `sort_order` INT UNSIGNED DEFAULT 0 COMMENT '排序',
    `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除:0-否,1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_is_deleted` (`is_deleted`),
    CONSTRAINT `fk_product_category_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品表
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
    `category_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '商品分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `subtitle` VARCHAR(255) DEFAULT NULL COMMENT '商品副标题',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '商品描述',
    `image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片',
    `images` VARCHAR(1000) DEFAULT NULL COMMENT '商品图片列表',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    `unit` VARCHAR(20) NOT NULL DEFAULT '份' COMMENT '单位',
    `stock` INT UNSIGNED DEFAULT NULL COMMENT '库存(NULL表示无限)',
    `sales_volume` INT UNSIGNED DEFAULT 0 COMMENT '销量',
    `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态:0-下架,1-上架',
    `sort_order` INT UNSIGNED DEFAULT 0 COMMENT '排序',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除:0-否,1-是',
    `version` INT UNSIGNED DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_is_deleted` (`is_deleted`),
    CONSTRAINT `fk_product_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `product_category` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 地址表
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '地址ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `name` VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `address` VARCHAR(255) NOT NULL COMMENT '详细地址',
    `is_default` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否默认:0-否,1-是',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除:0-否,1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_default` (`is_default`),
    KEY `idx_is_deleted` (`is_deleted`),
    CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地址表';

-- 订单表
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
    `order_type` TINYINT UNSIGNED DEFAULT 1 COMMENT '订单类型:1-普通订单',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `actual_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '订单备注',
    `delivery_address` VARCHAR(200) DEFAULT NULL COMMENT '配送地址',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
    `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态:1-待支付,2-已支付,3-已接单,4-配送中,5-已完成,6-已取消',
    `pay_status` TINYINT UNSIGNED DEFAULT 0 COMMENT '支付状态:0-未支付,1-已支付',
    `pay_method` TINYINT UNSIGNED DEFAULT NULL COMMENT '支付方式:1-微信支付,2-支付宝',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `accept_time` DATETIME DEFAULT NULL COMMENT '接单时间',
    `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除:0-否,1-是',
    `is_archived` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否归档:0-否,1-是',
    `archive_time` DATETIME DEFAULT NULL COMMENT '归档时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_status` (`status`),
    KEY `idx_pay_status` (`pay_status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_is_deleted` (`is_deleted`),
    KEY `idx_is_archived` (`is_archived`),
    CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON UPDATE CASCADE,
    CONSTRAINT `fk_order_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单项表
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称(快照)',
    `price` DECIMAL(10,2) NOT NULL COMMENT '商品单价(快照)',
    `product_image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片(快照)',
    `quantity` INT UNSIGNED NOT NULL COMMENT '数量',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`),
    CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_order_item_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';

-- 支付表
DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '支付ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `payment_no` VARCHAR(32) NOT NULL COMMENT '支付单号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    `channel` TINYINT UNSIGNED NOT NULL COMMENT '支付渠道:1-微信,2-支付宝',
    `status` TINYINT UNSIGNED DEFAULT 0 COMMENT '状态:0-未支付,1-支付中,2-已支付,3-已关闭',
    `trade_no` VARCHAR(64) DEFAULT NULL COMMENT '第三方支付交易号',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `callback_time` DATETIME DEFAULT NULL COMMENT '回调时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除:0-否,1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    UNIQUE KEY `uk_order_trade` (`order_id`, `trade_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_trade_no` (`trade_no`),
    KEY `idx_status` (`status`),
    KEY `idx_status_create_time` (`status`, `create_time`),
    KEY `idx_is_deleted` (`is_deleted`),
    CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付表';

-- 优惠券表
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
    `merchant_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '商家ID(NULL表示平台通用)',
    `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    `type` TINYINT UNSIGNED NOT NULL COMMENT '类型:1-满减券,2-折扣券',
    `min_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '最低消费金额',
    `discount_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '优惠金额(满减券)',
    `discount_percent` INT UNSIGNED DEFAULT NULL COMMENT '折扣百分比(折扣券,如90表示9折)',
    `total_count` INT UNSIGNED NOT NULL COMMENT '发放总量',
    `remain_count` INT UNSIGNED NOT NULL COMMENT '剩余数量',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除:0-否,1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`),
    KEY `idx_is_deleted` (`is_deleted`),
    CONSTRAINT `fk_coupon_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 用户优惠券表
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `coupon_id` BIGINT UNSIGNED NOT NULL COMMENT '优惠券ID',
    `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态:1-未使用,2-已使用,3-已过期',
    `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
    `order_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '使用的订单ID',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除:0-否,1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_coupon` (`user_id`, `coupon_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_coupon_id` (`coupon_id`),
    KEY `idx_status` (`status`),
    KEY `idx_is_deleted` (`is_deleted`),
    CONSTRAINT `fk_user_coupon_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_user_coupon_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- 评价表
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
    `product_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '商品ID(可选)',
    `rating` TINYINT UNSIGNED NOT NULL COMMENT '评分:1-5',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
    `images` VARCHAR(1000) DEFAULT NULL COMMENT '评价图片(逗号分隔)',
    `merchant_reply` VARCHAR(500) DEFAULT NULL COMMENT '商家回复',
    `reply_time` DATETIME DEFAULT NULL COMMENT '回复时间',
    `is_show` TINYINT UNSIGNED DEFAULT 1 COMMENT '是否展示:0-否,1-是',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除:0-否,1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_rating` (`rating`),
    KEY `idx_is_deleted` (`is_deleted`),
    CONSTRAINT `fk_review_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_review_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON UPDATE CASCADE,
    CONSTRAINT `fk_review_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON UPDATE CASCADE,
    CONSTRAINT `fk_review_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 通知表
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `type` TINYINT UNSIGNED NOT NULL COMMENT '类型:1-订单通知,2-系统通知',
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `content` VARCHAR(500) NOT NULL COMMENT '内容',
    `extra_data` VARCHAR(500) DEFAULT NULL COMMENT '额外数据(JSON)',
    `is_read` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否已读:0-否,1-是',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除:0-否,1-是',
    `delete_time` DATETIME DEFAULT NULL COMMENT '删除时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_is_deleted` (`is_deleted`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 文件表
DROP TABLE IF EXISTS `file`;
CREATE TABLE `file` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_name` VARCHAR(255) NOT NULL COMMENT '存储文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
    `file_url` VARCHAR(500) NOT NULL COMMENT '文件访问URL',
    `file_type` VARCHAR(50) NOT NULL COMMENT '文件类型',
    `file_size` BIGINT UNSIGNED NOT NULL COMMENT '文件大小(字节)',
    `mime_type` VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    `module` VARCHAR(50) DEFAULT NULL COMMENT '所属模块',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除:0-否,1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_module` (`module`),
    KEY `idx_file_type` (`file_type`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第二部分：初始化数据
-- ============================================

-- 初始化商家分类
INSERT INTO `merchant_category` (`id`, `name`, `icon`, `sort_order`) VALUES
(1, '美食', 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=100', 1),
(2, '快餐', 'https://images.unsplash.com/photo-1561758033-d8f236a78826?w=100', 2),
(3, '饮品', 'https://images.unsplash.com/photo-1544145945-f90425340c7e?w=100', 3),
(4, '小吃', 'https://images.unsplash.com/photo-1563379926898-05f4575a45d8?w=100', 4),
(5, '烘焙', 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=100', 5);

-- 密码：123456 (BCrypt加密)
-- BCrypt: $2a$10$TFHrS8Fy9vjlPtTN7BuLhuwM/o.bSEDmTAJNkMoZPeIc.9Gj0.nRa

-- 初始化用户（管理员、商家、普通用户）
INSERT INTO `user` (`id`, `username`, `nickname`, `phone`, `email`, `password`, `avatar`, `gender`, `user_type`, `merchant_id`, `status`, `last_login_time`, `last_login_ip`, `login_count`, `register_time`, `register_ip`) VALUES
(1, 'admin', '超级管理员', '13800000001', 'admin@campus.edu', '$2a$10$TFHrS8Fy9vjlPtTN7BuLhuwM/o.bSEDmTAJNkMoZPeIc.9Gj0.nRa', 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&h=200&fit=crop', 1, 2, NULL, 1, NOW(), '127.0.0.1', 100, '2025-09-01 00:00:00', '127.0.0.1'),
(2, 'zhangsan', '张三', '13800000002', 'zhangsan@campus.edu', '$2a$10$TFHrS8Fy9vjlPtTN7BuLhuwM/o.bSEDmTAJNkMoZPeIc.9Gj0.nRa', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&h=200&fit=crop', 1, 1, 1, 1, NOW(), '127.0.0.1', 50, '2025-09-01 00:00:00', '127.0.0.1'),
(3, 'lisi', '李四', '13800000003', 'lisi@campus.edu', '$2a$10$TFHrS8Fy9vjlPtTN7BuLhuwM/o.bSEDmTAJNkMoZPeIc.9Gj0.nRa', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&h=200&fit=crop', 1, 1, 2, 1, NOW(), '127.0.0.1', 45, '2025-09-02 00:00:00', '127.0.0.1'),
(4, 'wangwu', '王五', '13800000004', 'wangwu@campus.edu', '$2a$10$TFHrS8Fy9vjlPtTN7BuLhuwM/o.bSEDmTAJNkMoZPeIc.9Gj0.nRa', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200&h=200&fit=crop', 1, 1, 3, 1, NOW(), '127.0.0.1', 60, '2025-09-03 00:00:00', '127.0.0.1'),
(5, 'chenji', '陈记', '13800000005', 'chenji@campus.edu', '$2a$10$TFHrS8Fy9vjlPtTN7BuLhuwM/o.bSEDmTAJNkMoZPeIc.9Gj0.nRa', 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200&h=200&fit=crop', 2, 1, 4, 1, NOW(), '127.0.0.1', 55, '2025-09-04 00:00:00', '127.0.0.1'),
(6, 'shitang', '第一食堂', '13800000006', 'shitang@campus.edu', '$2a$10$TFHrS8Fy9vjlPtTN7BuLhuwM/o.bSEDmTAJNkMoZPeIc.9Gj0.nRa', 'https://images.unsplash.com/photo-1560250097-0b93528c311a?w=200&h=200&fit=crop', 1, 1, 5, 1, NOW(), '127.0.0.1', 80, '2025-09-05 00:00:00', '127.0.0.1'),
(7, 'student01', '王同学', '13800000011', 'student01@campus.edu', '$2a$10$TFHrS8Fy9vjlPtTN7BuLhuwM/o.bSEDmTAJNkMoZPeIc.9Gj0.nRa', 'https://images.unsplash.com/photo-1544725176-7c40e5a71c5e?w=200&h=200&fit=crop', 1, 0, NULL, 1, NOW(), '127.0.0.1', 30, '2025-09-10 00:00:00', '127.0.0.1'),
(8, 'student02', '李同学', '13800000012', 'student02@campus.edu', '$2a$10$TFHrS8Fy9vjlPtTN7BuLhuwM/o.bSEDmTAJNkMoZPeIc.9Gj0.nRa', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&h=200&fit=crop', 2, 0, NULL, 1, NOW(), '127.0.0.1', 25, '2025-09-11 00:00:00', '127.0.0.1'),
(9, 'student03', '张同学', '13800000013', 'student03@campus.edu', '$2a$10$TFHrS8Fy9vjlPtTN7BuLhuwM/o.bSEDmTAJNkMoZPeIc.9Gj0.nRa', 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200&h=200&fit=crop', 1, 0, NULL, 1, NOW(), '127.0.0.1', 20, '2025-09-12 00:00:00', '127.0.0.1'),
(10, 'student04', '赵同学', '13800000014', 'student04@campus.edu', '$2a$10$TFHrS8Fy9vjlPtTN7BuLhuwM/o.bSEDmTAJNkMoZPeIc.9Gj0.nRa', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop', 2, 0, NULL, 1, NOW(), '127.0.0.1', 15, '2025-09-13 00:00:00', '127.0.0.1'),
(11, 'student05', '刘同学', '13800000015', 'student05@campus.edu', '$2a$10$TFHrS8Fy9vjlPtTN7BuLhuwM/o.bSEDmTAJNkMoZPeIc.9Gj0.nRa', 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&h=200&fit=crop', 2, 0, NULL, 1, NOW(), '127.0.0.1', 18, '2025-09-14 00:00:00', '127.0.0.1');

-- 初始化商家
INSERT INTO `merchant` (`id`, `name`, `logo`, `banner`, `category_id`, `description`, `notice`, `phone`, `address`, `min_order_amount`, `avg_price`, `rating`, `sales_volume`, `status`, `sort_order`) VALUES
(1, '张三快餐店', 'https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=200&h=200&fit=crop', 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800&h=300&fit=crop', 1, '主营各类快餐便当，营养健康，价格实惠，是校园内最受欢迎的快餐店之一', '新店开业，全场8折优惠！满20元免配送费', '0571-88888801', '校园东区食堂二楼', 10.00, 15.00, 4.80, 1200, 1, 100),
(2, '李四小吃店', 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=200&h=200&fit=crop', 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=800&h=300&fit=crop', 2, '特色小吃，深夜食堂，营业至凌晨2点，满足你的夜宵需求', '营业至凌晨2点，夜宵首选！', '0571-88888802', '校园西区商业街3号', 5.00, 12.00, 4.50, 800, 1, 90),
(3, '王五川菜馆', 'https://images.unsplash.com/photo-1563245372-f21724e3856d?w=200&h=200&fit=crop', 'https://images.unsplash.com/photo-1506377247377-2a5b3b417ebb?w=800&h=300&fit=crop', 3, '正宗川菜，麻辣鲜香，招牌水煮鱼、麻婆豆腐不容错过', '招牌水煮鱼，不容错过！新客立减5元', '0571-88888803', '校园北区美食街5号', 20.00, 35.00, 4.90, 600, 1, 95),
(4, '陈记甜品', 'https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=200&h=200&fit=crop', 'https://images.unsplash.com/photo-1551024506-0bccd828d307?w=800&h=300&fit=crop', 4, '手工甜品，新鲜制作，每日限量供应，招牌杨枝甘露、芒果班戟', '每日限量供应，先到先得', '0571-88888804', '校园南区甜品街2号', 15.00, 20.00, 4.70, 500, 1, 80),
(5, '第一食堂', 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=200&h=200&fit=crop', 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800&h=300&fit=crop', 5, '校园第一食堂，经济实惠，支持校园卡支付，干净卫生', '支持校园卡支付，学生专属优惠', '0571-88888805', '校园中心区食堂一楼', 8.00, 10.00, 4.30, 2000, 1, 100);

-- 初始化商家营业时间
INSERT INTO `merchant_business_hours` (`merchant_id`, `day_of_week`, `meal_type`, `start_time`, `end_time`, `is_open`) VALUES
(1, 1, 1, '07:00', '09:00', 1), (1, 1, 2, '11:00', '13:30', 1), (1, 1, 3, '17:00', '20:00', 1),
(1, 2, 1, '07:00', '09:00', 1), (1, 2, 2, '11:00', '13:30', 1), (1, 2, 3, '17:00', '20:00', 1),
(1, 3, 1, '07:00', '09:00', 1), (1, 3, 2, '11:00', '13:30', 1), (1, 3, 3, '17:00', '20:00', 1),
(1, 4, 1, '07:00', '09:00', 1), (1, 4, 2, '11:00', '13:30', 1), (1, 4, 3, '17:00', '20:00', 1),
(1, 5, 1, '07:00', '09:00', 1), (1, 5, 2, '11:00', '13:30', 1), (1, 5, 3, '17:00', '20:00', 1),
(1, 6, 1, '07:00', '09:00', 1), (1, 6, 2, '11:00', '13:30', 1), (1, 6, 3, '17:00', '20:00', 1),
(1, 7, 1, '07:00', '09:00', 1), (1, 7, 2, '11:00', '13:30', 1), (1, 7, 3, '17:00', '20:00', 1),
(2, 1, 3, '18:00', '02:00', 1), (2, 2, 3, '18:00', '02:00', 1), (2, 3, 3, '18:00', '02:00', 1),
(2, 4, 3, '18:00', '02:00', 1), (2, 5, 3, '18:00', '02:00', 1), (2, 6, 3, '18:00', '02:00', 1), (2, 7, 3, '18:00', '02:00', 1),
(3, 1, 2, '11:00', '14:00', 1), (3, 1, 3, '17:00', '21:00', 1),
(3, 2, 2, '11:00', '14:00', 1), (3, 2, 3, '17:00', '21:00', 1),
(3, 3, 2, '11:00', '14:00', 1), (3, 3, 3, '17:00', '21:00', 1),
(3, 4, 2, '11:00', '14:00', 1), (3, 4, 3, '17:00', '21:00', 1),
(3, 5, 2, '11:00', '14:00', 1), (3, 5, 3, '17:00', '21:00', 1),
(3, 6, 2, '11:00', '14:00', 1), (3, 6, 3, '17:00', '21:00', 1),
(3, 7, 2, '11:00', '14:00', 1), (3, 7, 3, '17:00', '21:00', 1),
(4, 1, 2, '10:00', '18:00', 1), (4, 2, 2, '10:00', '18:00', 1), (4, 3, 2, '10:00', '18:00', 1),
(4, 4, 2, '10:00', '18:00', 1), (4, 5, 2, '10:00', '18:00', 1), (4, 6, 2, '10:00', '18:00', 1), (4, 7, 2, '10:00', '18:00', 1),
(5, 1, 1, '06:30', '09:30', 1), (5, 1, 2, '11:00', '13:00', 1), (5, 1, 3, '17:00', '19:30', 1),
(5, 2, 1, '06:30', '09:30', 1), (5, 2, 2, '11:00', '13:00', 1), (5, 2, 3, '17:00', '19:30', 1),
(5, 3, 1, '06:30', '09:30', 1), (5, 3, 2, '11:00', '13:00', 1), (5, 3, 3, '17:00', '19:30', 1),
(5, 4, 1, '06:30', '09:30', 1), (5, 4, 2, '11:00', '13:00', 1), (5, 4, 3, '17:00', '19:30', 1),
(5, 5, 1, '06:30', '09:30', 1), (5, 5, 2, '11:00', '13:00', 1), (5, 5, 3, '17:00', '19:30', 1),
(5, 6, 1, '07:00', '09:00', 1), (5, 6, 2, '11:00', '13:00', 1), (5, 6, 3, '17:00', '19:00', 1),
(5, 7, 1, '07:00', '09:00', 1), (5, 7, 2, '11:00', '13:00', 1), (5, 7, 3, '17:00', '19:00', 1);

-- 初始化商品分类
INSERT INTO `product_category` (`id`, `merchant_id`, `name`, `sort_order`) VALUES
(1, 1, '招牌套餐', 1),
(2, 1, '盖浇饭', 2),
(3, 1, '面食类', 3),
(4, 1, '饮品', 4),
(5, 2, '烧烤类', 1),
(6, 2, '炸物类', 2),
(7, 2, '饮品', 3),
(8, 3, '招牌菜', 1),
(9, 3, '川菜系列', 2),
(10, 3, '凉菜', 3),
(11, 3, '主食', 4),
(12, 4, '招牌甜品', 1),
(13, 4, '饮品', 2),
(14, 4, '蛋糕', 3),
(15, 5, '套餐', 1),
(16, 5, '单点', 2),
(17, 5, '饮品', 3);

-- 初始化商品
INSERT INTO `product` (`id`, `merchant_id`, `category_id`, `name`, `subtitle`, `description`, `image`, `images`, `price`, `original_price`, `unit`, `stock`, `sales_volume`, `status`, `sort_order`, `is_deleted`, `version`) VALUES
-- 张三快餐店商品
(1, 1, 1, '红烧肉套餐', '精选五花肉套餐', '红烧肉+时蔬+米饭+例汤，精选五花肉，慢炖2小时', 'https://images.unsplash.com/photo-1594041680534-e8c8cdebd659?w=400', NULL, 18.00, 28.00, '份', 100, 500, 1, 100, 0, 0),
(2, 1, 1, '宫保鸡丁套餐', '经典川味套餐', '宫保鸡丁+时蔬+米饭+例汤，花生米酥脆，鸡肉嫩滑', 'https://images.unsplash.com/photo-1525755662778-989d0524087e?w=400', NULL, 16.00, 25.00, '份', 100, 400, 1, 90, 0, 0),
(3, 1, 1, '糖醋排骨套餐', '酸甜口味套餐', '糖醋排骨+时蔬+米饭+例汤，外酥里嫩', 'https://images.unsplash.com/photo-1544025945-f90425340c7e?w=400', NULL, 20.00, 32.00, '份', 100, 350, 1, 85, 0, 0),
(4, 1, 2, '鱼香肉丝盖饭', '川味经典', '鱼香肉丝+米饭，配菜丰富，味道正宗', 'https://images.unsplash.com/photo-1563379926898-05f4575a45d8?w=400', NULL, 15.00, 22.00, '份', 100, 300, 1, 80, 0, 0),
(5, 1, 2, '麻婆豆腐盖饭', '麻辣下饭', '麻婆豆腐+米饭，麻辣鲜香，豆腐嫩滑', 'https://images.unsplash.com/photo-1582576163090-09d3b6f8a969?w=400', NULL, 12.00, 18.00, '份', 100, 250, 1, 70, 0, 0),
(6, 1, 2, '番茄炒蛋盖饭', '家常美味', '番茄炒蛋+米饭，家常味道，老少皆宜', 'https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=400', NULL, 10.00, 15.00, '份', 100, 200, 1, 60, 0, 0),
(7, 1, 3, '牛肉面', '手工拉面', '手工拉面+牛肉+青菜，汤底熬制4小时', 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400', NULL, 16.00, 25.00, '碗', 100, 200, 1, 60, 0, 0),
(8, 1, 3, '炸酱面', '老北京风味', '手工面条+炸酱+黄瓜丝，地道老北京味道', 'https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=400', NULL, 12.00, 18.00, '碗', 100, 150, 1, 50, 0, 0),
(9, 1, 4, '可乐', '冰镇饮料', '330ml，冰镇更爽', 'https://images.unsplash.com/photo-1527960471264-932f39eb5846?w=400', NULL, 3.00, 5.00, '瓶', 100, 100, 1, 40, 0, 0),
(10, 1, 4, '雪碧', '清爽饮料', '330ml，透心凉', 'https://images.unsplash.com/photo-1625772299848-391b6a87d7b3?w=400', NULL, 3.00, 5.00, '瓶', 100, 80, 1, 40, 0, 0),
-- 李四小吃店商品
(11, 2, 5, '羊肉串', '炭火烤串', '精选羊肉，炭火烤制，孜然味浓', 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=400', NULL, 3.00, 5.00, '串', 200, 800, 1, 100, 0, 0),
(12, 2, 5, '牛肉串', '炭火烤串', '精选牛肉，炭火烤制，配秘制酱料', 'https://images.unsplash.com/photo-1593560708920-61dd98c46a4e?w=400', NULL, 3.50, 6.00, '串', 200, 600, 1, 90, 0, 0),
(13, 2, 5, '烤鸡翅', '秘制鸡翅', '鸡翅中，秘制腌制24小时，炭火烤制', 'https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=400', NULL, 6.00, 10.00, '个', 100, 400, 1, 80, 0, 0),
(14, 2, 5, '烤韭菜', '素食烤串', '新鲜韭菜，炭火烤制，配秘制酱料', 'https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400', NULL, 5.00, 8.00, '份', 100, 300, 1, 70, 0, 0),
(15, 2, 6, '炸鸡块', '金黄酥脆', '黄金炸鸡块，配番茄酱', 'https://images.unsplash.com/photo-1626645738196-c2a7c87a8f58?w=400', NULL, 8.00, 12.00, '份', 100, 300, 1, 70, 0, 0),
(16, 2, 6, '薯条', '快餐必备', '现炸薯条，配番茄酱', 'https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=400', NULL, 6.00, 10.00, '份', 100, 250, 1, 60, 0, 0),
(17, 2, 6, '炸鸡翅', '香脆鸡翅', '整只鸡翅，现炸现卖', 'https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=400', NULL, 8.00, 12.00, '个', 100, 200, 1, 50, 0, 0),
(18, 2, 7, '啤酒', '冰镇啤酒', '500ml，冰镇更爽', 'https://images.unsplash.com/photo-1608270586620-248524c67de9?w=400', NULL, 5.00, 8.00, '瓶', 50, 200, 1, 50, 0, 0),
(19, 2, 7, '可乐', '经典可乐', '330ml', 'https://images.unsplash.com/photo-1527960471264-932f39eb5846?w=400', NULL, 3.00, 5.00, '瓶', 100, 150, 1, 40, 0, 0),
-- 王五川菜馆商品
(20, 3, 8, '水煮鱼', '招牌川菜', '精选草鱼，配豆芽、莴笋，麻辣鲜香', 'https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?w=400', NULL, 68.00, 98.00, '份', 50, 400, 1, 100, 0, 0),
(21, 3, 8, '毛血旺', '麻辣经典', '鸭血、毛肚、午餐肉、豆芽等，麻辣鲜香', 'https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=400', NULL, 58.00, 88.00, '份', 50, 300, 1, 90, 0, 0),
(22, 3, 9, '回锅肉', '正宗川味', '五花肉+蒜苗+豆瓣酱，正宗川味', 'https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?w=400', NULL, 38.00, 58.00, '份', 50, 250, 1, 80, 0, 0),
(23, 3, 9, '辣子鸡', '麻辣干香', '鸡丁+干辣椒+花生米，麻辣干香', 'https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=400', NULL, 42.00, 62.00, '份', 50, 200, 1, 70, 0, 0),
(24, 3, 9, '鱼香肉丝', '鱼香味浓', '猪肉丝+木耳+胡萝卜，鱼香味浓', 'https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?w=400', NULL, 32.00, 48.00, '份', 50, 180, 1, 60, 0, 0),
(25, 3, 10, '凉拌黄瓜', '清爽凉菜', '新鲜黄瓜，蒜泥调味', 'https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=400', NULL, 12.00, 18.00, '份', 50, 150, 1, 50, 0, 0),
(26, 3, 10, '凉拌木耳', '养生凉菜', '黑木耳，蒜泥调味', 'https://images.unsplash.com/photo-1540420773420-3366772f4999?w=400', NULL, 15.00, 22.00, '份', 50, 120, 1, 40, 0, 0),
(27, 3, 11, '米饭', '东北大米', '东北大米，粒粒分明', 'https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400', NULL, 2.00, 3.00, '碗', 200, 500, 1, 30, 0, 0),
-- 陈记甜品商品
(28, 4, 12, '杨枝甘露', '招牌甜品', '芒果+西米+椰浆+柚子粒，清爽可口', 'https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=400', NULL, 22.00, 32.00, '份', 50, 350, 1, 100, 0, 0),
(29, 4, 12, '芒果班戟', '港式甜品', '新鲜芒果+奶油+薄饼皮，甜而不腻', 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400', NULL, 18.00, 28.00, '份', 50, 280, 1, 90, 0, 0),
(30, 4, 12, '双皮奶', '顺德甜品', '水牛奶制作，口感细腻', 'https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=400', NULL, 12.00, 18.00, '份', 50, 200, 1, 80, 0, 0),
(31, 4, 13, '芒果汁', '鲜榨果汁', '新鲜芒果榨汁，无添加', 'https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400', NULL, 15.00, 22.00, '杯', 50, 150, 1, 70, 0, 0),
(32, 4, 13, '椰汁', '天然饮品', '新鲜椰子，现开现喝', 'https://images.unsplash.com/photo-1525385133512-2f3bdd039054?w=400', NULL, 10.00, 15.00, '个', 50, 100, 1, 60, 0, 0),
(33, 4, 14, '提拉米苏', '意式甜品', '马斯卡彭芝士+咖啡酒，口感丰富', 'https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=400', NULL, 28.00, 38.00, '份', 30, 80, 1, 50, 0, 0),
(34, 4, 14, '芒果蛋糕', '生日蛋糕', '新鲜芒果+奶油蛋糕', 'https://images.unsplash.com/photo-1563729784474-d77dbb933a9e?w=400', NULL, 32.00, 45.00, '份', 30, 60, 1, 40, 0, 0),
-- 第一食堂商品
(35, 5, 15, '一荤两素套餐', '经济实惠', '一荤两素+米饭+例汤，学生首选', 'https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400', NULL, 12.00, 18.00, '份', 200, 800, 1, 100, 0, 0),
(36, 5, 15, '两荤两素套餐', '丰盛套餐', '两荤两素+米饭+例汤，份量足', 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=400', NULL, 15.00, 22.00, '份', 200, 600, 1, 90, 0, 0),
(37, 5, 15, '一荤一素套餐', '简单实惠', '一荤一素+米饭，简单实惠', 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400', NULL, 10.00, 15.00, '份', 200, 500, 1, 80, 0, 0),
(38, 5, 16, '红烧肉', '经典菜品', '肥而不腻，入口即化', 'https://images.unsplash.com/photo-1594041680534-e8c8cdebd659?w=400', NULL, 8.00, 12.00, '份', 100, 300, 1, 70, 0, 0),
(39, 5, 16, '番茄炒蛋', '家常菜', '家常味道，老少皆宜', 'https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=400', NULL, 5.00, 8.00, '份', 100, 250, 1, 60, 0, 0),
(40, 5, 16, '青椒肉丝', '下饭神器', '青椒+肉丝，下饭必备', 'https://images.unsplash.com/photo-1563379926898-05f4575a45d8?w=400', NULL, 6.00, 10.00, '份', 100, 200, 1, 50, 0, 0),
(41, 5, 17, '豆浆', '营养早餐', '现磨豆浆，营养健康', 'https://images.unsplash.com/photo-1563805042-7684c019e1cb?w=400', NULL, 2.00, 3.00, '杯', 200, 400, 1, 40, 0, 0),
(42, 5, 17, '绿豆汤', '清热解暑', '绿豆汤，清热解暑', 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400', NULL, 1.50, 2.50, '碗', 200, 300, 1, 30, 0, 0);

-- ============================================
-- 说明：
-- 1. 本脚本整合了V5、V6、V7的所有变更
-- 2. 添加了version字段支持乐观锁
-- 3. 添加了幂等性唯一索引uk_order_trade
-- 4. 优化了字段默认值和注释
-- 5. 统一了字段命名规范
-- ============================================
