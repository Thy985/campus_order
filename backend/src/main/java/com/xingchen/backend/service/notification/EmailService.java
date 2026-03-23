package com.xingchen.backend.service.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮件服务类
 * 用于发送各类邮件，包括验证码邮件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from.address:1850833838@qq.com}")
    private String fromAddress;

    @Value("${mail.from.name:校园点餐系统}")
    private String fromName;

    @Value("${mail.verify-code.subject:校园点餐系统 - 验证码}")
    private String verifyCodeSubject;

    /**
     * 发送验证码邮件
     *
     * @param email 收件人邮箱
     * @param code  验证码
     */
    public void sendVerifyCodeEmail(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress, fromName);
            helper.setTo(email);
            helper.setSubject(verifyCodeSubject);
            helper.setText(buildVerifyCodeHtml(code), true);

            mailSender.send(message);
            log.info("验证码邮件已发送至: {}", email);
        } catch (MessagingException e) {
            log.error("发送验证码邮件失败, 邮箱: {}, 错误: {}", email, e.getMessage());
            throw new RuntimeException("发送验证码邮件失败", e);
        } catch (Exception e) {
            log.error("发送验证码邮件时发生未知错误, 邮箱: {}, 错误: {}", email, e.getMessage());
            throw new RuntimeException("发送验证码邮件失败", e);
        }
    }

    /**
     * 构建验证码邮件HTML内容
     *
     * @param code 验证码
     * @return HTML格式的邮件内容
     */
    private String buildVerifyCodeHtml(String code) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>校园点餐系统 - 验证码</title>
                <style>
                    body {
                        font-family: 'Microsoft YaHei', Arial, sans-serif;
                        background-color: #f5f5f5;
                        margin: 0;
                        padding: 20px;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #ffffff;
                        border-radius: 8px;
                        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                        font-weight: 500;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .greeting {
                        font-size: 16px;
                        color: #333;
                        margin-bottom: 20px;
                    }
                    .code-container {
                        background-color: #f8f9fa;
                        border: 2px dashed #667eea;
                        border-radius: 8px;
                        padding: 30px;
                        text-align: center;
                        margin: 25px 0;
                    }
                    .code-label {
                        font-size: 14px;
                        color: #666;
                        margin-bottom: 10px;
                    }
                    .code {
                        font-size: 36px;
                        font-weight: bold;
                        color: #667eea;
                        letter-spacing: 8px;
                        font-family: 'Courier New', monospace;
                    }
                    .info {
                        background-color: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .info p {
                        margin: 0;
                        font-size: 14px;
                        color: #856404;
                    }
                    .warning {
                        background-color: #f8d7da;
                        border-left: 4px solid #dc3545;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .warning p {
                        margin: 0;
                        font-size: 14px;
                        color: #721c24;
                    }
                    .footer {
                        background-color: #f8f9fa;
                        padding: 20px 30px;
                        text-align: center;
                        border-top: 1px solid #e9ecef;
                    }
                    .footer p {
                        margin: 5px 0;
                        font-size: 12px;
                        color: #6c757d;
                    }
                    .footer .brand {
                        font-weight: bold;
                        color: #667eea;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>校园点餐系统</h1>
                    </div>
                    <div class="content">
                        <p class="greeting">您好！</p>
                        <p class="greeting">您正在进行身份验证，请使用以下验证码完成操作：</p>
                        
                        <div class="code-container">
                            <div class="code-label">您的验证码</div>
                            <div class="code">%s</div>
                        </div>
                        
                        <div class="info">
                            <p>⏰ 验证码有效期为 <strong>5 分钟</strong>，请尽快使用。</p>
                        </div>
                        
                        <div class="warning">
                            <p>⚠️ 安全提示：请勿将验证码告知他人，工作人员不会向您索取验证码。如非本人操作，请忽略此邮件。</p>
                        </div>
                    </div>
                    <div class="footer">
                        <p class="brand">校园点餐系统</p>
                        <p>此邮件由系统自动发送，请勿回复</p>
                        <p>如有疑问，请联系客服</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(code);
    }
}
