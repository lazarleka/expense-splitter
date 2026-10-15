package com.coinis.splitter.service;

import static com.coinis.splitter.TestData.user;
import static com.coinis.splitter.TestData.withId;
import static com.coinis.splitter.service.InvitationServiceTest.assertStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coinis.splitter.model.Event;
import com.coinis.splitter.model.EventMember;
import com.coinis.splitter.model.User;
import com.coinis.splitter.repository.EventMemberRepository;
import com.coinis.splitter.repository.EventRepository;
import com.coinis.splitter.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

class EventServiceTest {

    private final EventRepository events = mock(EventRepository.class);
    private final EventMemberRepository members = mock(EventMemberRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final EventService service = new EventService(events, members, users);

    private final User ana = user(1, "ana");

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
    }
}
