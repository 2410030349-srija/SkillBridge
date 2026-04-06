package com.skillbridge.skillbridge.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 120) String password,
        @NotBlank @Size(max = 30) String role,
        @Size(max = 500) String bio,
        List<String> teachSkills,
        List<String> learnSkills
) {
}
