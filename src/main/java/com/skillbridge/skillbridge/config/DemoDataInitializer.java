package com.skillbridge.skillbridge.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.skillbridge.skillbridge.model.User;
import com.skillbridge.skillbridge.service.UserService;

@Configuration
public class DemoDataInitializer {

    private static final String DEFAULT_PASSWORD = "Password123!";
    private static final String STUDENT_ROLE = "STUDENT";

    @Bean
    CommandLineRunner loadDemoData(UserService userService) {
        return args -> {
            if (!userService.getAllUsers().isEmpty()) {
                return;
            }

            User user1 = new User();
            user1.setName("Aditi Sharma");
            user1.setEmail("aditi@example.com");
            user1.setPassword(DEFAULT_PASSWORD);
            user1.setRole(STUDENT_ROLE);
            user1.setBio("Frontend learner who enjoys design and UI polish.");
            user1.setTeachSkills(List.of("html", "css"));
            user1.setLearnSkills(List.of("javascript", "public speaking"));

            User user2 = new User();
            user2.setName("Rohan Patel");
            user2.setEmail("rohan@example.com");
            user2.setPassword(DEFAULT_PASSWORD);
            user2.setRole(STUDENT_ROLE);
            user2.setBio("Coding mentor focusing on JavaScript and app structure.");
            user2.setTeachSkills(List.of("javascript", "react"));
            user2.setLearnSkills(List.of("design", "communication"));

            User user3 = new User();
            user3.setName("Neha Verma");
            user3.setEmail("neha@example.com");
            user3.setPassword(DEFAULT_PASSWORD);
            user3.setRole(STUDENT_ROLE);
            user3.setBio("Language exchange partner focused on English fluency.");
            user3.setTeachSkills(List.of("english", "communication"));
            user3.setLearnSkills(List.of("react", "figma"));

            userService.saveUser(user1);
            userService.saveUser(user2);
            userService.saveUser(user3);
        };
    }
}