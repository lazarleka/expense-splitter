package com.coinis.splitter.api.dto;

import com.coinis.splitter.model.Avatar;
import jakarta.validation.constraints.NotNull;

/** Vrijednost van skupa Avatar Jackson odbija pri citanju, pa je odgovor 400. */
public record AvatarRequest(@NotNull Avatar avatar) {
}
