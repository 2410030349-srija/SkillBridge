package com.skillbridge.skillbridge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DomainUpdateRequest(
        @NotBlank @Size(max = 40) String domain
) {
}
