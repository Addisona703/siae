package com.hngy.siae.content.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时调度清理任务
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemCleanupTask {

    private final SystemCleanupService systemCleanupService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void executeAllCleanup() {
        systemCleanupService.cleanTrashContents();
        systemCleanupService.cleanDeletedContents();
        systemCleanupService.cleanDeletedCategory();
    }
}

