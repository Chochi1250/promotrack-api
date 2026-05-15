package com.promotrack.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class InternalDemoControllerProfileTest {

    @Test
    void internalDemoControllerIsAvailableInDevProfile() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles("dev");
            context.register(InternalDemoController.class);
            context.refresh();

            assertThat(context.getBeansOfType(InternalDemoController.class)).hasSize(1);
        }
    }

    @Test
    void internalDemoControllerIsNotAvailableOutsideDevProfile() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles("prod");
            context.register(InternalDemoController.class);
            context.refresh();

            assertThat(context.getBeansOfType(InternalDemoController.class)).isEmpty();
        }
    }
}
