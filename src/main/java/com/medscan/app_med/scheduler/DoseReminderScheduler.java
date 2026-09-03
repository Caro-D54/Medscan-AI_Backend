package com.medscan.app_med.scheduler;

import com.medscan.app_med.service.DoseReminderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DoseReminderScheduler {

    private final DoseReminderService doseReminderService;

    public DoseReminderScheduler(DoseReminderService doseReminderService) {
        this.doseReminderService = doseReminderService;
    }

    @Scheduled(fixedDelayString = "${notification.reminder.delay-ms:60000}")
    public void remindDueDoses() {
        doseReminderService.sendDueReminders();
    }
}
