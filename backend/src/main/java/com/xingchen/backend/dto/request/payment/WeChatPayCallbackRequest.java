package com.xingchen.backend.dto.request.payment;

import lombok.Data;

/**
 * 微信支付回调请求
 * 
 * @author 小跃
 * @date 2026-02-13
 */
@Data
public class WeChatPayCallbackRequest {
    
    /**
     * 通知ID
     */
    private String id;
    
    /**
     * 通知创建时间
     */
    private String createTime;
    
    /**
     * 通知类型
     */
    private String eventType;
    
    /**
     * 通知数据类型
     */
    private String resourceType;
    
    /**
     * 通知数据
     */
    private Resource resource;
    
    /**
     * 回调摘要
     */
    private String summary;
    
    @Data
    public static class Resource {
        /**
         * 加密算法类型
         */
        private String algorithm;
        
        /**
         * 数据密文
         */
        private String ciphertext;
        
        /**
         * 附加数据
         */
        private String associatedData;
        
        /**
         * 原始类型
         */
        private String originalType;
        
        /**
         * 随机?         */
        private String nonce;
    }
}
