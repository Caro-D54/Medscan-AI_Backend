package com.medscan.app_med.scheduler;

import com.medscan.app_med.service.DoseReminderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DoseReminderSchedulerTest {

    @Mock
    private DoseReminderService doseReminderService;

    @InjectMocks
    private DoseReminderScheduler doseReminderScheduler;

    @Test
    void remindDueDosesDelegatesToService() {
        doseReminderScheduler.remindDueDoses();

        verify(doseReminderService).sendDueReminders();
    }

    @Test
    void remindDueDosesHasSchedulerLockAnnotation() throws NoSuchMethodException {
        var method = DoseReminderScheduler.class.getMethod("remindDueDoses");
        var lockAnnotation = method.getAnnotation(net.javacrumbs.shedlock.spring.annotation.SchedulerLock.class);

        org.junit.jupiter.api.Assertions.assertNotNull(lockAnnotation, "Debe tener @SchedulerLock para proteger contra concurrencia");
        org.junit.jupiter.api.Assertions.assertEquals("DoseReminderScheduler_remindDueDoses", lockAnnotation.name());
        org.junit.jupiter.api.Assertions.assertEquals("30s", lockAnnotation.lockAtLeastFor());
        org.junit.jupiter.api.Assertions.assertEquals("5m", lockAnnotation.lockAtMostFor());
    }
}
