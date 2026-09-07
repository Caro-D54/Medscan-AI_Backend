package com.medscan.app_med.service;

import com.medscan.app_med.model.Dose;
import com.medscan.app_med.model.DoseStatus;
import com.medscan.app_med.model.Medicament;
import com.medscan.app_med.model.Treatment;
import com.medscan.app_med.model.User;
import com.medscan.app_med.notification.PushNotification;
import com.medscan.app_med.notification.PushNotificationSender;
import com.medscan.app_med.repository.DoseRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoseReminderServiceTest {

    @Mock
    private DoseRepo doseRepo;

    @Mock
    private PushNotificationSender pushNotificationSender;

    private DoseReminderService doseReminderService;

    @BeforeEach
    void setUp() {
        doseReminderService = new DoseReminderService(doseRepo, pushNotificationSender, 10);
    }

    private Dose doseFor(DoseStatus status, String pushToken, String medicationName) {
        User user = new User();
        user.setId(1L);
        user.setPushToken(pushToken);

        Medicament medicament = new Medicament();
        medicament.setName(medicationName);

        Treatment treatment = new Treatment();
        treatment.setUser(user);
        treatment.setMedicament(medicament);

        Dose dose = new Dose();
        dose.setId(10L);
        dose.setStatus(status);
        dose.setScheduledAt(LocalDateTime.now().plusMinutes(2));
        dose.setTreatment(treatment);
        return dose;
    }

    @Test
    void sendsReminderForEachDuePendingDoseWithToken() {
        Dose first = doseFor(DoseStatus.PENDING, "expo-token-1", "Acetaminophen");
        Dose second = doseFor(DoseStatus.PENDING, "expo-token-2", "Ibuprofen");
        when(doseRepo.findDueToNotify(any(), any())).thenReturn(List.of(first, second));

        int sent = doseReminderService.sendDueReminders();

        assertThat(sent).isEqualTo(2);
        verify(pushNotificationSender, times(2)).send(any(PushNotification.class));

        ArgumentCaptor<PushNotification> captor = ArgumentCaptor.forClass(PushNotification.class);
        verify(pushNotificationSender, times(2)).send(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(PushNotification::token)
                .containsExactly("expo-token-1", "expo-token-2");
        assertThat(captor.getAllValues())
                .extracting(PushNotification::body)
                .containsExactly("Toma Acetaminophen", "Toma Ibuprofen");

        assertThat(first.getNotifiedAt()).isNotNull();
        assertThat(second.getNotifiedAt()).isNotNull();
    }

    @Test
    void skipsDosesWhoseUserHasNoPushToken() {
        Dose withToken = doseFor(DoseStatus.PENDING, "expo-token-1", "Acetaminophen");
        Dose withoutToken = doseFor(DoseStatus.PENDING, null, "Ibuprofen");
        Dose blankToken = doseFor(DoseStatus.PENDING, "  ", "Aspirin");
        when(doseRepo.findDueToNotify(any(), any())).thenReturn(List.of(withToken, withoutToken, blankToken));

        int sent = doseReminderService.sendDueReminders();

        assertThat(sent).isEqualTo(1);
        verify(pushNotificationSender, times(1)).send(any(PushNotification.class));
        assertThat(withToken.getNotifiedAt()).isNotNull();
        assertThat(withoutToken.getNotifiedAt()).isNull();
        assertThat(blankToken.getNotifiedAt()).isNull();
    }

    @Test
    void sendsNothingWhenNoDueDoses() {
        when(doseRepo.findDueToNotify(any(), any())).thenReturn(List.of());

        int sent = doseReminderService.sendDueReminders();

        assertThat(sent).isZero();
        verify(pushNotificationSender, never()).send(any(PushNotification.class));
    }
}
