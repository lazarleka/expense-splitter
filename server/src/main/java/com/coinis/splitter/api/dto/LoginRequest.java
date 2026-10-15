package com.coinis.splitter.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String username, @NotBlank String password) {

    // Lozinka ne smije zavrsiti u logu
    @Override
    public String toString() {
        return "LoginRequest[username=" + username + "]";
    }
}
