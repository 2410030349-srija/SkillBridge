package com.skillbridge.skillbridge.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileSetupRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Size(max = 40) String domain,
        @NotBlank @Size(max = 30) String role,
        List<String> interests,
        @Size(max = 500) String bio
) {
}
