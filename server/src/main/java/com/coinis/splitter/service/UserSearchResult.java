package com.coinis.splitter.service;

import com.coinis.splitter.model.User;

/**
 * Korisnik iz pretrage i njegov odnos prema korisniku koji trazi.
 * requestId postoji samo za zahtjev na cekanju (poslat ili primljen).
 */
public record UserSearchResult(User user, Relation relation, Long requestId) {

    public enum Relation { NONE, FRIEND, OUTGOING, INCOMING }
}
