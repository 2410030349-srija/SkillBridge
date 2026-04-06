package com.skillbridge.skillbridge.dto;

import com.skillbridge.skillbridge.model.RequestStatus;

import jakarta.validation.constraints.NotNull;

public record RequestStatusUpdate(
        @NotNull RequestStatus status
) {
}