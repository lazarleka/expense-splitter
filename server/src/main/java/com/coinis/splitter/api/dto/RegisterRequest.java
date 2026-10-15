package com.coinis.splitter.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 50) String username,
        @NotNull @Size(min = 8, max = 100) String password) {

    // Lozinka ne smije zavrsiti u logu
    @Override
    public String toString() {
        return "RegisterRequest[username=" + username + "]";
    }
}
