package com.skillbridge.skillbridge.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoDataInitializer {

    @Bean
    CommandLineRunner loadDemoData() {
        return args -> {
            // Demo data disabled - only real registered users will be stored
            // Keeping this bean as a no-op avoids accidental demo data insertion.
        };
    }
}