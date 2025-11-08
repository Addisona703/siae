package com.hngy.siae.media.worker;

import com.hngy.siae.media.service.LifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 生命周期管理Worker
 * 定期执行文件生命周期策略
 *
 * @author SIAE Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LifecycleWorker {

    private final LifecycleService lifecycleService;

    /**
     * 执行生命周期策略
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void executeLifecyclePolicies() {
        log.info("Starting lifecycle worker");
        
        try {
            lifecycleService.executeLifecyclePolicies();
        } catch (Exception e) {
            log.error("Lifecycle worker failed", e);
        }
        
        log.info("Lifecycle worker completed");
    }

}
