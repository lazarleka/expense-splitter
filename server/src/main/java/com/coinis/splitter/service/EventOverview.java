package com.coinis.splitter.service;

import com.coinis.splitter.model.Event;
import com.coinis.splitter.model.EventMember;
import com.coinis.splitter.model.Expense;
import com.coinis.splitter.settlement.Transfer;
import java.util.List;

/**
 * Sve za prikaz dogadjaja. balances[i] je stanje clana members.get(i) (red ulaska).
 */
public record EventOverview(
        Event event,
        List<EventMember> members,
        long[] balances,
        List<Expense> expenses,
        long totalCents,
        List<Transfer> transfers) {
}
