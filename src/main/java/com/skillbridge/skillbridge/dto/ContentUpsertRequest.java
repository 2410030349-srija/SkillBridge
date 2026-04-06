package com.skillbridge.skillbridge.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContentUpsertRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 1500) String description,
        @NotBlank @Size(max = 40) String domain,
        List<String> tags,
        @Size(max = 500) String resourceLink,
        @Size(max = 500) String resourceFile
) {
}
