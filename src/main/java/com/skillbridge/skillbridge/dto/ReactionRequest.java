package com.skillbridge.skillbridge.dto;

import jakarta.validation.constraints.NotBlank;

public record ReactionRequest(
        @NotBlank String type
) {
}
