package com.jk.blog.config;

import com.jk.blog.service.scheduler.DataCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@Configuration
@EnableScheduling
public class ScheduledTasksConfig {

    @Autowired
    private DataCleanupService dataCleanupService;

    // cleanup job runs every day at 1 AM.
    @Scheduled(cron = "0 0 1 * * *")
    public void performDataCleanupTask() {
        this.dataCleanupService.cleanupDeactivatedUserAccounts();
    }
}
