package com.skillbridge.skillbridge.dto; // NOSONAR - false positive: package is named

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
        @NotNull Long reviewerId,
        @NotNull Long reviewedUserId,
        @NotNull @Min(1) @Max(5) Integer rating,
        @NotBlank String feedback
) {
}