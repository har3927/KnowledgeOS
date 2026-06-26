package com.knowledgeos.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "knowledgeos.ai.openai")
@Getter @Setter
public class OpenAiProperties {
    private String baseUrl;
    private String model;
    private String apiKey;
    private boolean enabled;
    private int timeoutSeconds;
}
