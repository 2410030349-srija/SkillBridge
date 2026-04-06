package com.skillbridge.skillbridge.dto; // NOSONAR - false positive: package is named

import jakarta.validation.constraints.NotBlank;

public record SkillRequest(
        @NotBlank(message = "Skill is required") String skill
) {
}