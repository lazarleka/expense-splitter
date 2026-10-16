package com.coinis.splitter.service;

import com.coinis.splitter.model.Event;

/** Dogadjaj za listu na pocetnoj strani, iz ugla jednog korisnika. */
public record EventCard(Event event, int memberCount, long totalCents, long myBalanceCents) {
}
