package com.knowledgeos.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KnowledgeOsApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeOsApplication.class, args);
    }
}
