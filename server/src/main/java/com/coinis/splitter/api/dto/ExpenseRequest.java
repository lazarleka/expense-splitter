package com.coinis.splitter.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ExpenseRequest(
        @NotBlank @Size(max = 100) String title,
        @Positive long amountCents,
        @NotNull Long payerId) {
}
