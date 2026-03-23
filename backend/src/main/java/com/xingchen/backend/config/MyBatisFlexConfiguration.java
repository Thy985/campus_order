package com.xingchen.backend.config;

import com.mybatisflex.core.audit.AuditManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MyBatisFlexConfiguration {

    static {
        AuditManager.setAuditEnable(true);

        AuditManager.setMessageCollector(auditMessage -> {
            log.info("SQL Execution: {}, Time: {}ms",
                auditMessage.getFullSql(),
                auditMessage.getElapsedTime());
        });

        log.info("MyBatis-Flex Configuration Initialized");
    }
}
