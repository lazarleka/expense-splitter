package com.coinis.splitter.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParticipantRequest(
        @NotBlank @Size(max = 50) String name) {
}
