package com.medscan.app_med.controller;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HomeControllerTest {

    private final HomeController controller = new HomeController();

    @Test
    void indexReturnsHomeView() {
        assertThat(controller.index()).isEqualTo("home");
    }

    @Test
    void dashboardReturnsUserView() {
        assertThat(controller.dashboard()).isEqualTo("user");
    }
}
