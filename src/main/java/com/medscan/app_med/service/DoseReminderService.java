package com.medscan.app_med.service;

import com.medscan.app_med.model.Dose;
import com.medscan.app_med.model.DoseStatus;
import com.medscan.app_med.model.User;
import com.medscan.app_med.notification.PushNotification;
import com.medscan.app_med.notification.PushNotificationSender;
import com.medscan.app_med.repository.DoseRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DoseReminderService {

    private static final String REMINDER_TITLE = "Hora de tu medicamento";

    private final DoseRepo doseRepo;
    private final PushNotificationSender pushNotificationSender;
    private final int advanceMinutes;

    public DoseReminderService(DoseRepo doseRepo,
                               PushNotificationSender pushNotificationSender,
                               @Value("${notification.reminder.advance-minutes:10}") int advanceMinutes) {
        this.doseRepo = doseRepo;
        this.pushNotificationSender = pushNotificationSender;
        this.advanceMinutes = advanceMinutes;
    }

    @Transactional
    public int sendDueReminders() {
        LocalDateTime deadline = LocalDateTime.now().plusMinutes(advanceMinutes);
        List<Dose> dueDoses = doseRepo.findDueToNotify(DoseStatus.PENDING, deadline);
        int sent = 0;
        for (Dose dose : dueDoses) {
            User user = dose.getTreatment().getUser();
            if (hasPushToken(user)) {
                pushNotificationSender.send(buildNotification(dose, user));
                dose.setNotifiedAt(LocalDateTime.now());
                sent++;
            }
        }
        return sent;
    }

    private boolean hasPushToken(User user) {
        return user != null && user.getPushToken() != null && !user.getPushToken().isBlank();
    }

    private PushNotification buildNotification(Dose dose, User user) {
        String medicationName = dose.getTreatment().getMedicament().getName();
        return new PushNotification(user.getPushToken(), REMINDER_TITLE, "Toma " + medicationName);
    }
}
