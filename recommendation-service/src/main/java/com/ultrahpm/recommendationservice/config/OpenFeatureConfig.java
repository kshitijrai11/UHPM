package com.ultrahpm.recommendationservice.config;

import dev.openfeature.sdk.providers.memory.InMemoryProvider;
import dev.openfeature.sdk.providers.memory.Flag;
import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.OpenFeatureAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OpenFeatureConfig {

    @Bean
    public Client openFeatureClient() {
        Map<String, Flag<?>> flags = new HashMap<>();
        // Default to true. Turn this to false to disable the heavy ONNX engine.
        flags.put("ai-recommendations-enabled", Flag.builder()
                .variant("on", true)
                .defaultVariant("on")
                .build());

        InMemoryProvider provider = new InMemoryProvider(flags);
        OpenFeatureAPI.getInstance().setProviderAndWait(provider);

        return OpenFeatureAPI.getInstance().getClient();
    }
}
