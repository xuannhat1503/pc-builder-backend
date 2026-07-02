package com.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRoleUpdateRequest(
        @NotBlank @Size(max = 50) String role
) {
}