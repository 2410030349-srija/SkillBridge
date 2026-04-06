package com.skillbridge.skillbridge.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.skillbridge.dto.ContentView;
import com.skillbridge.skillbridge.service.ContentPlatformService;

@RestController
@RequestMapping("/home")
public class HomeController {

    private final ContentPlatformService contentPlatformService;

    public HomeController(ContentPlatformService contentPlatformService) {
        this.contentPlatformService = contentPlatformService;
    }

    @GetMapping("/feed")
    public List<ContentView> feed(Authentication authentication) {
        return contentPlatformService.personalizedFeed(authentication.getName());
    }
}
