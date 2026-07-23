package com.ultrahpm.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
@RefreshScope
public class DynamicConfiguration {

    @Value("${app.rate-limit.requests-per-minute:100}")
    private int rateLimit;

    @Value("${app.feature.new-registration:false}")
    private boolean newRegistrationEnabled;

    @GetMapping("/current")
    public String getCurrentConfig() {
        return String.format(
            "Current Configuration -> Rate Limit: %d/min, New Registration Enabled: %b",
            rateLimit, newRegistrationEnabled
        );
    }
}
