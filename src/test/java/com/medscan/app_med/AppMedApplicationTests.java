package com.medscan.app_med;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "app.security.username=test",
        "app.security.password=test"
})
class AppMedApplicationTests {

    @Test
    void contextLoads() {
    }
}