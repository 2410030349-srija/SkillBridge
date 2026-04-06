package com.skillbridge.skillbridge.dto; // NOSONAR - false positive: package is named

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatMessageRequest(
        @NotNull Long senderId,
        @NotBlank String message
) {
}