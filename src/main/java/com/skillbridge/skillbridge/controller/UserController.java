package com.skillbridge.skillbridge.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.skillbridge.skillbridge.dto.SkillRequest;
import com.skillbridge.skillbridge.model.User;
import com.skillbridge.skillbridge.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Create user
    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        return userService.saveUser(user);
    }

    // Get all users
    @GetMapping
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    // Get user by email
    @GetMapping("/email/{email}")
    public User getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    @PostMapping("/{email}/teach-skills")
    public User addTeachSkill(@PathVariable String email, @Valid @RequestBody SkillRequest request, Authentication authentication) {
        ensureCurrentUser(email, authentication);
        return userService.addTeachSkill(authentication.getName(), request.skill());
    }

    @PostMapping("/{email}/learn-skills")
    public User addLearnSkill(@PathVariable String email, @Valid @RequestBody SkillRequest request, Authentication authentication) {
        ensureCurrentUser(email, authentication);
        return userService.addLearnSkill(authentication.getName(), request.skill());
    }

    @GetMapping("/{email}/matches")
    public List<User> getMatches(@PathVariable String email, Authentication authentication) {
        ensureCurrentUser(email, authentication);
        return userService.findMatchesForUser(authentication.getName());
    }

    @GetMapping("/search")
    public List<User> searchBySkill(@RequestParam String skill) {
        return userService.searchUsersBySkill(skill);
    }

    private void ensureCurrentUser(String email, Authentication authentication) {
        if (authentication == null || !authentication.getName().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("You can only access your own profile");
        }
    }
}