package com.medscan.app_med.service;

import com.medscan.app_med.model.Dose;
import com.medscan.app_med.model.DoseStatus;
import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.model.User;
import com.medscan.app_med.notification.PushNotification;
import com.medscan.app_med.notification.PushNotificationSender;
import com.medscan.app_med.repository.DoseRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DoseReminderService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DoseReminderService.class);

    private static final String REMINDER_TITLE = "Hora de tu medicamento";
    private static final String REMINDER_CATEGORY_ID = "dose-reminder";
    private static final String REMINDER_CHANNEL_ID = "dose-reminders";

    private final DoseRepo doseRepo;
    private final PushNotificationSender pushNotificationSender;
    private final int advanceMinutes;

    public DoseReminderService(DoseRepo doseRepo,
                               PushNotificationSender pushNotificationSender,
                               @Value("${notification.reminder.advance-minutes:${app.notification.reminder.advance-minutes:10}}") int advanceMinutes) {
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
                try {
                    pushNotificationSender.send(buildNotification(dose, user));
                    dose.setNotifiedAt(LocalDateTime.now());
                    doseRepo.save(dose);
                    sent++;
                } catch (Exception ex) {
                    log.error("Error al enviar notificación push para la dosis ID {}: {}", dose.getId(), ex.getMessage());
                }
            }
        }
        return sent;
    }

    private boolean hasPushToken(User user) {
        return user != null && user.getPushToken() != null && !user.getPushToken().isBlank();
    }

    private PushNotification buildNotification(Dose dose, User user) {
        Medicament medicament = dose.getTreatment().getMedicament();
        String medicationName = medicament.getName();
        Map<String, Object> data = new LinkedHashMap<>();
        if (dose.getId() != null) {
            data.put("doseId", String.valueOf(dose.getId()));
        }
        if (medicament.getId() != null) {
            data.put("medicationId", String.valueOf(medicament.getId()));
            data.put("url", "/medication/" + medicament.getId());
        }
        if (dose.getTreatment().getId() != null) {
            data.put("treatmentId", String.valueOf(dose.getTreatment().getId()));
        }
        return new PushNotification(
                user.getPushToken(),
                REMINDER_TITLE,
                "Toma " + medicationName,
                REMINDER_CATEGORY_ID,
                REMINDER_CHANNEL_ID,
                data
        );
    }
}

