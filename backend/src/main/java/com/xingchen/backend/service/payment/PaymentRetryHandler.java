package com.xingchen.backend.service.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 支付重试处理器
 * 用于支付接口调用失败时的重试机制
 *
 * @author 小跃
 * @date 2026-02-14
 */
@Slf4j
@Component
public class PaymentRetryHandler {

    /**
     * 默认最大重试次数
     */
    private static final int DEFAULT_MAX_RETRIES = 3;

    /**
     * 默认重试间隔(毫秒)
     */
    private static final long DEFAULT_RETRY_INTERVAL = 1000;

    /**
     * 执行带重试的操作
     *
     * @param operation    操作名称
     * @param task         要执行的任务
     * @param maxRetries   最大重试次数
     * @param retryInterval 重试间隔(毫秒)
     * @param <T>          返回类型
     * @return 执行结果
     * @throws Exception 执行失败异常
     */
    public <T> T executeWithRetry(String operation, Supplier<T> task, int maxRetries, long retryInterval) throws Exception {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                attempt++;
                log.info("执行操作: {}, 第{}次尝试", operation, attempt);
                
                T result = task.get();
                
                if (attempt > 1) {
                    log.info("操作成功: {}, 重试{}次后成功", operation, attempt - 1);
                }
                
                return result;
                
            } catch (Exception e) {
                lastException = e;
                log.warn("操作失败: {}, 第{}次尝试失败: {}", operation, attempt, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        // 指数退避策略，每次重试间隔翻倍
                        long waitTime = retryInterval * (long) Math.pow(2, attempt - 1);
                        log.info("等待{}毫秒后重试...", waitTime);
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new Exception("重试被中断", ie);
                    }
                }
            }
        }

        log.error("操作最终失败: {}, 已重试{}次", operation, maxRetries);
        throw new Exception("操作失败，已达最大重试次数: " + maxRetries, lastException);
    }

    /**
     * 执行带重试的操作(使用默认参数)
     *
     * @param operation 操作名称
     * @param task      要执行的任务
     * @param <T>       返回类型
     * @return 执行结果
     * @throws Exception 执行失败异常
     */
    public <T> T executeWithRetry(String operation, Supplier<T> task) throws Exception {
        return executeWithRetry(operation, task, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_INTERVAL);
    }

    /**
     * 执行带重试的操作(无返回值)
     *
     * @param operation    操作名称
     * @param task         要执行的任务
     * @param maxRetries   最大重试次数
     * @param retryInterval 重试间隔(毫秒)
     * @throws Exception 执行失败异常
     */
    public void executeWithRetryVoid(String operation, Runnable task, int maxRetries, long retryInterval) throws Exception {
        executeWithRetry(operation, () -> {
            task.run();
            return null;
        }, maxRetries, retryInterval);
    }

    /**
     * 执行带重试的操作(无返回值，使用默认参数)
     *
     * @param operation 操作名称
     * @param task      要执行的任务
     * @throws Exception 执行失败异常
     */
    public void executeWithRetryVoid(String operation, Runnable task) throws Exception {
        executeWithRetryVoid(operation, task, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_INTERVAL);
    }

    /**
     * 判断异常是否可重试
     *
     * @param e 异常
     * @return 是否可重试
     */
    public boolean isRetryableException(Exception e) {
        // 网络异常、超时异常等可重试
        String message = e.getMessage();
        if (message == null) {
            return false;
        }

        // 可重试的异常类型
        return message.contains("timeout") ||
               message.contains("connect") ||
               message.contains("network") ||
               message.contains("503") ||
               message.contains("502") ||
               message.contains("504");
    }

    /**
     * 执行带条件重试的操作(只重试特定异常)
     *
     * @param operation    操作名称
     * @param task         要执行的任务
     * @param maxRetries   最大重试次数
     * @param retryInterval 重试间隔(毫秒)
     * @param <T>          返回类型
     * @return 执行结果
     * @throws Exception 执行失败异常
     */
    public <T> T executeWithConditionalRetry(String operation, Supplier<T> task, int maxRetries, long retryInterval) throws Exception {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                attempt++;
                log.info("执行操作: {}, 第{}次尝试", operation, attempt);
                
                T result = task.get();
                
                if (attempt > 1) {
                    log.info("操作成功: {}, 重试{}次后成功", operation, attempt - 1);
                }
                
                return result;
                
            } catch (Exception e) {
                lastException = e;
                log.warn("操作失败: {}, 第{}次尝试失败: {}", operation, attempt, e.getMessage());
                
                // 判断是否可重试
                if (!isRetryableException(e)) {
                    log.error("不可重试的异常，直接抛出: {}", e.getMessage());
                    throw e;
                }
                
                if (attempt < maxRetries) {
                    try {
                        long waitTime = retryInterval * (long) Math.pow(2, attempt - 1);
                        log.info("等待{}毫秒后重试...", waitTime);
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new Exception("重试被中断", ie);
                    }
                }
            }
        }

        log.error("操作最终失败: {}, 已重试{}次", operation, maxRetries);
        throw new Exception("操作失败，已达最大重试次数: " + maxRetries, lastException);
    }
}
