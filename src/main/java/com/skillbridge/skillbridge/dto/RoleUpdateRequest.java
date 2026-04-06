package com.skillbridge.skillbridge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleUpdateRequest(
        @NotBlank @Size(max = 30) String role
) {
}
