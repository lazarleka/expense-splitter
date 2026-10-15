package com.coinis.splitter.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EventRequest(@NotBlank @Size(max = 100) String name) {
}
