package com.coinis.splitter.api.dto;

import jakarta.validation.constraints.NotNull;

public record ArchiveConsentRequest(@NotNull Boolean consent) {
}
