package com.medscan.app_med.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class ExpoPushNotificationSenderTest {

    @Test
    void sendPostsExpoPayloadToConfiguredUrl() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ExpoPushNotificationSender sender = new ExpoPushNotificationSender(
                new ObjectMapper(), builder, "https://exp.host/--/api/v2/push/send");

        server.expect(once(), requestTo("https://exp.host/--/api/v2/push/send"))
                .andExpect(method(POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        [{"to":"expo-token-1","title":"Hora de tu medicamento","body":"Toma Acetaminophen"}]
                        """))
                .andRespond(withSuccess());

        sender.send(new PushNotification("expo-token-1", "Hora de tu medicamento", "Toma Acetaminophen"));

        server.verify();
    }
}
