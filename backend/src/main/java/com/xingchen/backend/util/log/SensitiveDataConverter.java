package com.xingchen.backend.util.log;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Logback敏感数据脱敏转换器
 * 在日志输出时自动脱敏敏感信息
 */
public class SensitiveDataConverter extends MessageConverter {
    
    @Override
    public String convert(ILoggingEvent event) {
        String message = super.convert(event);
        return SensitiveDataUtil.maskSensitiveData(message);
    }
}
