package com.coinis.splitter.settlement;

/**
 * Trosak za obracun: iznos u centima, id clana koji je platio i broj clanova u trenutku unosa.
 * Trosak se dijeli jednako na prvih memberCount clanova po redu ulaska; clan koji se pridruzi
 * kasnije ne ucestvuje u ranijim troskovima.
 */
public record PaidAmount(long amountCents, long payerId, int memberCount) {
}
