package com.xingchen.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 校园点餐管理系统后端服务启动类
 *
 * <p>主要功能模块：</p>
 * <ul>
 *   <li>用户管理 - 用户注册、登录、个人信息管理</li>
 *   <li>商户管理 - 商户入驻、店铺管理、商品管理</li>
 *   <li>订单管理 - 订单创建、支付、状态跟踪</li>
 *   <li>支付系统 - 支付宝沙箱支付集成</li>
 *   <li>实时通知 - WebSocket订单状态推送</li>
 *   <li>缓存系统 - Redis缓存与分布式锁</li>
 * </ul>
 *
 * <p>技术栈：Spring Boot 3.x + MyBatis-Flex + Sa-Token + Redis</p>
 *
 * @author xingchen
 * @version 1.0.0
 */
@EnableScheduling
@SpringBootApplication
@MapperScan("com.xingchen.backend.mapper")
public class BackendApplication {

    /**
     * 应用程序入口
     *
     * <p>启动时自动执行：</p>
     * <ul>
     *   <li>数据库连接池初始化</li>
     *   <li>Redis连接初始化</li>
     *   <li>定时任务注册（订单超时自动取消）</li>
     *   <li>MyBatis-Flex Mapper扫描</li>
     * </ul>
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
