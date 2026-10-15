package com.coinis.splitter.service;

import static com.coinis.splitter.TestData.event;
import static com.coinis.splitter.TestData.expense;
import static com.coinis.splitter.TestData.member;
import static com.coinis.splitter.TestData.user;
import static com.coinis.splitter.TestData.withId;
import static com.coinis.splitter.service.InvitationServiceTest.assertStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coinis.splitter.model.Event;
import com.coinis.splitter.model.EventMember;
import com.coinis.splitter.model.Expense;
import com.coinis.splitter.model.User;
import com.coinis.splitter.repository.EventMemberRepository;
import com.coinis.splitter.repository.EventRepository;
import com.coinis.splitter.repository.ExpenseRepository;
import com.coinis.splitter.repository.UserRepository;
import com.coinis.splitter.settlement.Transfer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

class EventServiceTest {

    private final EventRepository events = mock(EventRepository.class);
    private final EventMemberRepository members = mock(EventMemberRepository.class);
    private final ExpenseRepository expenses = mock(ExpenseRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final EventService service = new EventService(events, members, expenses, users);

    private final User ana = user(1, "ana");
    private final User marko = user(2, "marko");
    private final User iva = user(3, "iva");
    private final Event trip = event(10, "Izlet", ana);

    @Test
    void creatorBecomesFirstMember() {
        when(users.getReferenceById(1L)).thenReturn(ana);
        when(events.save(any(Event.class))).thenAnswer(i -> withId(i.<Event>getArgument(0), 10));

        Event event = service.create(1, "  Izlet ");

        assertThat(event.getName()).isEqualTo("Izlet");
        assertThat(event.getCreator()).isEqualTo(ana);
        assertThat(event.isArchived()).isFalse();
        ArgumentCaptor<EventMember> saved = ArgumentCaptor.forClass(EventMember.class);
        verify(members).save(saved.capture());
        assertThat(saved.getValue().getUser()).isEqualTo(ana);
        assertThat(saved.getValue().getEvent()).isEqualTo(event);
    }

    // Scenarij "Pristup": tudji i nepostojeci dogadjaj daju isti odgovor
    @Test
    void nonMemberGetsNotFound() {
        assertStatus(() -> service.requireMember(10, 3), HttpStatus.NOT_FOUND);
        assertStatus(() -> service.overview(10, 3), HttpStatus.NOT_FOUND);
        assertStatus(() -> service.addExpense(10, 3, "Rucak", 9000, 3), HttpStatus.NOT_FOUND);
    }

    // Scenarij "Rucak i taksi": ukupno 120,00; Marko duguje Ani 10,00; Iva duguje Ani 40,00
    @Test
    void lunchAndTaxiOverview() {
        givenMembers(ana, marko, iva);
        when(expenses.findAllByEventIdOrderByIdAsc(10L)).thenReturn(List.of(
                expense(1, trip, 9000, ana, 3), expense(2, trip, 3000, marko, 3)));

        EventOverview overview = service.overview(10, 1);

        assertThat(overview.totalCents()).isEqualTo(12000);
        assertThat(overview.balances()).containsExactly(5000, -1000, -4000);
        assertThat(overview.transfers()).containsExactly(new Transfer(2, 1, 1000), new Transfer(3, 1, 4000));
    }

    // Scenarij "Kasniji clan": trosak pamti koliko je clanova bilo u trenutku unosa
    @Test
    void expenseRemembersMembersAtTimeOfEntry() {
        givenMembers(ana, marko);
        when(expenses.save(any(Expense.class))).thenAnswer(i -> i.getArgument(0));

        Expense lunch = service.addExpense(10, 1, " Rucak ", 6000, 1);

        assertThat(lunch.getTitle()).isEqualTo("Rucak");
        assertThat(lunch.getMemberCount()).isEqualTo(2);
        assertThat(lunch.getPayer()).isEqualTo(ana);
    }

    @Test
    void laterMemberOverview() {
        givenMembers(ana, marko, iva);
        when(expenses.findAllByEventIdOrderByIdAsc(10L)).thenReturn(List.of(
                expense(1, trip, 6000, ana, 2), expense(2, trip, 3000, marko, 3)));

        EventOverview overview = service.overview(10, 3);

        assertThat(overview.balances()).containsExactly(2000, -1000, -1000);
        assertThat(overview.transfers()).containsExactly(new Transfer(2, 1, 1000), new Transfer(3, 1, 1000));
    }

    // Scenarij "Validacija unosa": platilac koji nije clan
    @Test
    void payerMustBeMember() {
        givenMembers(ana, marko);

        assertStatus(() -> service.addExpense(10, 1, "Rucak", 9000, 3), HttpStatus.BAD_REQUEST);
        verify(expenses, never()).save(any());
    }

    @Test
    void archivedEventRejectsExpense() {
        givenMembers(ana, marko);
        trip.archive();

        assertStatus(() -> service.addExpense(10, 1, "Rucak", 9000, 1), HttpStatus.CONFLICT);
        verify(expenses, never()).save(any());
    }

    private void givenMembers(User... ordered) {
        List<EventMember> list = new ArrayList<>();
        for (int i = 0; i < ordered.length; i++) {
            EventMember m = member(100 + i, trip, ordered[i]);
            list.add(m);
            when(members.findByEventIdAndUserId(10L, ordered[i].getId())).thenReturn(Optional.of(m));
        }
        when(members.findAllByEventIdOrderByIdAsc(10L)).thenReturn(list);
    }
}
