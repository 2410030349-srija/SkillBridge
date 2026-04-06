package com.skillbridge.skillbridge.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.skillbridge.dto.ProfileSetupRequest;
import com.skillbridge.skillbridge.dto.RoleUpdateRequest;
import com.skillbridge.skillbridge.dto.DomainUpdateRequest;
import com.skillbridge.skillbridge.model.User;
import com.skillbridge.skillbridge.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/profile")
@Validated
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/setup")
    public User setupProfile(@Valid @RequestBody ProfileSetupRequest request, Authentication authentication) {
        return userService.setupProfile(
                authentication.getName(),
                request.fullName(),
                request.domain(),
                request.role(),
            request.interests(),
            request.bio());
    }

    @PostMapping("/verify-email")
    public User verifyEmail(Authentication authentication) {
        return userService.markEmailVerified(authentication.getName());
    }

    @PutMapping("/role")
    public User updateRole(@Valid @RequestBody RoleUpdateRequest request, Authentication authentication) {
        return userService.changeOwnRole(authentication.getName(), request.role());
    }

    @PutMapping("/domain")
    public User updateDomain(@Valid @RequestBody DomainUpdateRequest request, Authentication authentication) {
        return userService.changeOwnDomain(authentication.getName(), request.domain());
    }

    @GetMapping("/me")
    public User me(Authentication authentication) {
        return userService.requireUserByEmail(authentication.getName());
    }

    @GetMapping("/domains")
    public List<String> domains() {
        return userService.supportedDomains();
    }
}
