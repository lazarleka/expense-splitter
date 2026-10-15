package com.coinis.splitter.api.dto;

/** Stanje u centima: pozitivno potrazuje, negativno duguje. */
public record MemberResponse(long userId, String username, long balanceCents, boolean archiveConsent) {
}
