package com.medscan.app_med.scheduler;

import net.javacrumbs.shedlock.core.LockProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class ShedLockIntegrationTest {

    @Autowired
    private LockProvider lockProvider;

    @Test
    void lockProviderBeanIsLoaded() {
        assertNotNull(lockProvider, "El bean LockProvider de ShedLock debe inicializarse en el contexto de Spring");
    }
}
