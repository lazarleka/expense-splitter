package com.coinis.splitter.service;

import com.coinis.splitter.model.Event;
import com.coinis.splitter.model.EventMember;
import com.coinis.splitter.model.Expense;
import com.coinis.splitter.model.User;
import com.coinis.splitter.repository.EventMemberRepository;
import com.coinis.splitter.repository.EventRepository;
import com.coinis.splitter.repository.ExpenseRepository;
import com.coinis.splitter.repository.UserRepository;
import com.coinis.splitter.settlement.PaidAmount;
import com.coinis.splitter.settlement.SettlementCalculator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EventService {

    private final EventRepository events;
    private final EventMemberRepository members;
    private final ExpenseRepository expenses;
    private final UserRepository users;

    public EventService(EventRepository events, EventMemberRepository members, ExpenseRepository expenses,
            UserRepository users) {
        this.events = events;
        this.members = members;
        this.expenses = expenses;
        this.users = users;
    }

    /** Dogadjaji u kojima je korisnik clan, po redu ulaska. */
    public List<Event> eventsOf(long userId) {
        return members.findAllByUserIdOrderByIdAsc(userId).stream()
                .map(EventMember::getEvent)
                .toList();
    }

    /** Kreator postaje prvi clan, pa je prvi i po redu ulaska. */
    @Transactional
    public Event create(long creatorId, String name) {
        User creator = users.getReferenceById(creatorId);
        Event event = events.save(new Event(name.trim(), creator));
        members.save(new EventMember(event, creator));
        return event;
    }

    /**
     * Clanstvo korisnika u dogadjaju. Za nepostojeci i za tudji dogadjaj vraca isti 404,
     * da se ne otkrije da dogadjaj postoji.
     */
    public EventMember requireMember(long eventId, long userId) {
        return members.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dogadjaj ne postoji"));
    }

    /** Clanovi po redu ulaska. */
    public List<EventMember> members(long eventId) {
        return members.findAllByEventIdOrderByIdAsc(eventId);
    }

    /** Trosak dijele clanovi u trenutku unosa; zato se pamti njihov broj. */
    @Transactional
    public Expense addExpense(long eventId, long userId, String title, long amountCents, long payerId) {
        Event event = requireMember(eventId, userId).getEvent();
        requireActive(event);

        List<EventMember> current = members(eventId);
        User payer = current.stream()
                .map(EventMember::getUser)
                .filter(u -> u.getId().equals(payerId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Platilac nije clan dogadjaja"));

        return expenses.save(new Expense(event, title.trim(), amountCents, payer, current.size()));
    }

    public EventOverview overview(long eventId, long userId) {
        Event event = requireMember(eventId, userId).getEvent();
        List<EventMember> ordered = members(eventId);
        List<Expense> costs = expenses.findAllByEventIdOrderByIdAsc(eventId);

        List<Long> memberIds = ordered.stream().map(m -> m.getUser().getId()).toList();
        List<PaidAmount> paid = costs.stream()
                .map(e -> new PaidAmount(e.getAmountCents(), e.getPayer().getId(), e.getMemberCount()))
                .toList();
        long total = costs.stream().mapToLong(Expense::getAmountCents).sum();

        return new EventOverview(event, ordered, SettlementCalculator.balances(memberIds, paid), costs, total,
                SettlementCalculator.calculate(memberIds, paid));
    }

    static void requireActive(Event event) {
        if (event.isArchived()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Dogadjaj je arhiviran");
        }
    }
}
