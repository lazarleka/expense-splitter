package com.coinis.splitter.settlement;

/**
 * Jedno dugovanje: ucesnik fromId duguje ucesniku toId iznos u centima.
 */
public record Transfer(long fromId, long toId, long amountCents) {
}
