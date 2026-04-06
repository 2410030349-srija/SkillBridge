package com.skillbridge.skillbridge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateExchangeRequest(
        @NotNull Long senderId,
        @NotNull Long receiverId,
        @NotBlank String requestedSkill,
        @NotBlank String note
) {
}