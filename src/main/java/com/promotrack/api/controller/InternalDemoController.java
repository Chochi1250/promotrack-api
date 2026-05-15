package com.promotrack.api.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
public class InternalDemoController {

    @GetMapping("/internal/demo/error")
    public void simulateInternalError() {
        throw new IllegalStateException("Intentional demo error for monitoring");
    }
}
