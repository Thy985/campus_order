package com.xingchen.backend.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.xingchen.backend.scheduler.OrderTimeoutScheduler;
import com.xingchen.backend.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/scheduler")
@RequiredArgsConstructor
@Tag(name = "Scheduler Management", description = "Manual scheduler trigger endpoints (for testing)")
@SaCheckRole("admin")
public class SchedulerController {

    private final OrderTimeoutScheduler orderTimeoutScheduler;

    @PostMapping("/order-timeout/trigger")
    @Operation(summary = "Trigger order timeout check", description = "Execute order timeout check immediately")
    public Result<String> triggerOrderTimeoutCheck() {
        log.info("Manual trigger order timeout check");

        try {
            orderTimeoutScheduler.triggerManually();
            return Result.success("Order timeout check triggered", null);
        } catch (Exception e) {
            log.error("Trigger order timeout check failed", e);
            return Result.error(500, "Trigger failed: " + e.getMessage());
        }
    }
}
