package com.knowledgeos.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "knowledgeos.ai.ollama")
@Getter @Setter
public class OllamaProperties {
    private String baseUrl = "http://localhost:11434";
    private String model = "qwen2.5:7b";
    private boolean enabled = true;
    /** Request timeout in seconds (local models can be slow on first load). */
    private int timeoutSeconds = 120;
}
