package com.knowledgeos.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "knowledgeos.ai.gemini")
@Getter @Setter
public class GeminiProperties {
    private String apiKey;
    private String model = "gemini-2.0-flash";
    private boolean enabled = true;
}
