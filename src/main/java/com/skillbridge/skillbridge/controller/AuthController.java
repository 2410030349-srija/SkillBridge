package com.skillbridge.skillbridge.controller;

import java.util.ArrayList;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.skillbridge.dto.AuthLoginRequest;
import com.skillbridge.skillbridge.dto.AuthRegisterRequest;
import com.skillbridge.skillbridge.dto.AuthResponse;
import com.skillbridge.skillbridge.model.User;
import com.skillbridge.skillbridge.service.JwtService;
import com.skillbridge.skillbridge.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody AuthRegisterRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setRole(request.role());
        user.setBio(request.bio());
        user.setTeachSkills(request.teachSkills() == null ? new ArrayList<>() : request.teachSkills());
        user.setLearnSkills(request.learnSkills() == null ? new ArrayList<>() : request.learnSkills());

        User created = userService.saveUser(user);
        String token = jwtService.generateToken(created.getEmail(), created.getRole());
        return new AuthResponse(token, created.getEmail(), created.getRole(), created.getId());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthLoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userService.requireUserByEmail(request.email());
        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getEmail(), user.getRole(), user.getId());
    }
}
