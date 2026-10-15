package com.coinis.splitter.api.dto;

/** Zahtjev na cekanju; user je drugi korisnik (posiljalac za primljene, primalac za poslate). */
public record FriendRequestResponse(long id, UserResponse user) {
}
