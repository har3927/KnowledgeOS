package com.knowledgeos.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "knowledgeos")
@Getter @Setter
public class AppProperties {
    private long defaultUserId = 1L;
}
