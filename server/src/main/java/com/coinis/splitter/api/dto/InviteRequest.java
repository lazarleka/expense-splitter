package com.coinis.splitter.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InviteRequest(@NotBlank @Size(max = 50) String username) {
}
