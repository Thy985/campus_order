package com.xingchen.backend.service.sms;

import java.util.List;
import java.util.Map;

/**
 * 短信发送服务接口
 * 使用阿里云短信服务发送短信
 */
public interface SmsService {
    
    /**
     * 发送短信
     * @param phone        手机号
     * @param templateCode 模板代码
     * @param params       模板参数
     * @return 是否发送成功
     */
    boolean sendSms(String phone, String templateCode, Map<String, String> params);
    
    /**
     * 发送验证码短信
     * @param phone 手机号
     * @param code  验证码
     * @return 是否发送成功
     */
    boolean sendVerifyCode(String phone, String code);
    
    /**
     * 发送订单状态通知
     * @param phone   手机号
     * @param orderNo 订单号
     * @param amount  金额
     * @return 是否发送成功
     */
    boolean sendOrderNotification(String phone, String orderNo, String amount);
    
    /**
     * 发送订单状态变更通知
     * @param phone      手机号
     * @param orderNo    订单号
     * @param statusText 状态文本
     * @return 是否发送成功
     */
    boolean sendOrderStatusChangeNotification(String phone, String orderNo, String statusText);
    
    /**
     * 发送新订单通知(给商家)
     * @param phone   手机号
     * @param orderNo 订单号
     * @param amount  金额
     * @return 是否发送成功
     */
    boolean sendNewOrderNotification(String phone, String orderNo, String amount);
    
    /**
     * 发送支付成功通知
     * @param phone   手机号
     * @param orderNo 订单号
     * @param amount  金额
     * @return 是否发送成功
     */
    boolean sendPaymentSuccessNotification(String phone, String orderNo, String amount);
    
    /**
     * 发送订单超时通知
     * @param phone   手机号
     * @param orderNo 订单号
     * @return 是否发送成功
     */
    boolean sendOrderTimeoutNotification(String phone, String orderNo);
    
    /**
     * 批量发送短信
     * @param phones       手机号列表
     * @param templateCode 模板代码
     * @param params       模板参数
     * @return 成功发送的数量
     */
    int sendBatchSms(String[] phones, String templateCode, Map<String, String> params);
    
    /**
     * 批量发送短信(使用List)
     * @param phones       手机号列表
     * @param templateCode 模板代码
     * @return 是否发送成功
     */
    boolean batchSendSms(List<String> phones, String templateCode);
}
