package com.coinis.splitter.api.dto;

import com.coinis.splitter.model.Avatar;

/** Stanje u centima: pozitivno potrazuje, negativno duguje. */
public record MemberResponse(long userId, String username, Avatar avatar, long balanceCents, boolean archiveConsent) {
}
