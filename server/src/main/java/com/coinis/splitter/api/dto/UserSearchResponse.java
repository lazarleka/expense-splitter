package com.coinis.splitter.api.dto;

/**
 * Rezultat pretrage. relation je NONE, FRIEND, OUTGOING ili INCOMING;
 * requestId postoji samo za OUTGOING i INCOMING (za povlacenje ili prihvatanje).
 */
public record UserSearchResponse(long id, String username, String relation, Long requestId) {
}
