package com.coinis.splitter.api.dto;

/** myBalanceCents je stanje prijavljenog korisnika: pozitivno potrazuje, negativno duguje. */
public record EventSummaryResponse(
        long id,
        String name,
        String creator,
        int memberCount,
        long totalCents,
        long myBalanceCents) {
}
