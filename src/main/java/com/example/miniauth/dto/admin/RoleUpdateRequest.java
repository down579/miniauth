package com.example.miniauth.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RoleUpdateRequest(
        @NotBlank
        @Pattern(regexp = "ROLE_[A-Z_]+")
        String roleCode
) {
}
