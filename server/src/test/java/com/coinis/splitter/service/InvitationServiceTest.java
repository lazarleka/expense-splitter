package com.coinis.splitter.service;

import static com.coinis.splitter.TestData.event;
import static com.coinis.splitter.TestData.invitation;
import static com.coinis.splitter.TestData.member;
import static com.coinis.splitter.TestData.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coinis.splitter.model.Event;
import com.coinis.splitter.model.EventMember;
import com.coinis.splitter.model.Invitation;
import com.coinis.splitter.model.User;
import com.coinis.splitter.repository.EventMemberRepository;
import com.coinis.splitter.repository.EventRepository;
import com.coinis.splitter.repository.ExpenseRepository;
import com.coinis.splitter.repository.InvitationRepository;
import com.coinis.splitter.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

class InvitationServiceTest {

    private final InvitationRepository invitations = mock(InvitationRepository.class);
    private final EventMemberRepository members = mock(EventMemberRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final EventService eventService = new EventService(mock(EventRepository.class), members,
            mock(ExpenseRepository.class), users);
    private final FriendService friendService = mock(FriendService.class);
    private final InvitationService service = new InvitationService(invitations, members, users, eventService,
            friendService);

    private final User ana = user(1, "ana");
    private final User marko = user(2, "marko");
    private final User iva = user(3, "iva");
    private final User petar = user(4, "petar");
    private final Event trip = event(10, "Izlet", ana);

    @BeforeEach
    void anaCreatedTheEvent() {
        when(members.findByEventIdAndUserId(10L, 1L)).thenReturn(Optional.of(member(100, trip, ana)));
        when(users.findByUsername("marko")).thenReturn(Optional.of(marko));
        when(users.findByUsername("iva")).thenReturn(Optional.of(iva));
        when(users.findByUsername("petar")).thenReturn(Optional.of(petar));
        // Marko i Iva su Anini prijatelji, Petar nije
        when(friendService.areFriends(1, 2)).thenReturn(true);
        when(friendService.areFriends(1, 3)).thenReturn(true);
        when(invitations.save(any(Invitation.class))).thenAnswer(i -> i.getArgument(0));
        when(members.save(any(EventMember.class))).thenAnswer(i -> i.getArgument(0));
    }

    // Scenarij "Pozivnica": Marko prihvata, Iva odbija; clanovi su Ana i Marko
    @Test
    void acceptedInvitationMakesMemberAndDeclinedDoesNot() {
        assertThat(service.invite(10, 1, "marko").getInvitee()).isEqualTo(marko);
        assertThat(service.invite(10, 1, " iva ").getInvitee()).isEqualTo(iva);

        Invitation forMarko = invitation(200, trip, marko);
        Invitation forIva = invitation(201, trip, iva);
        when(invitations.findById(200L)).thenReturn(Optional.of(forMarko));
        when(invitations.findById(201L)).thenReturn(Optional.of(forIva));

        EventMember newMember = service.accept(200, 2);
        service.decline(201, 3);

        assertThat(newMember.getUser()).isEqualTo(marko);
        assertThat(forMarko.getStatus()).isEqualTo(Invitation.Status.ACCEPTED);
        assertThat(forIva.getStatus()).isEqualTo(Invitation.Status.DECLINED);
        verify(members, never()).save(org.mockito.ArgumentMatchers.argThat(m -> m.getUser() == iva));
        // Iva nije clan, pa za nju dogadjaj ne postoji
        assertStatus(() -> eventService.requireMember(10, 3), HttpStatus.NOT_FOUND);
    }

    // Scenarij "Arhiviranje": novi clan ponistava date saglasnosti
    @Test
    void acceptingInvitationResetsArchiveConsents() {
        EventMember anaMember = member(100, trip, ana);
        anaMember.setArchiveConsent(true);
        when(members.findAllByEventIdOrderByIdAsc(10L)).thenReturn(List.of(anaMember));
        when(invitations.findById(200L)).thenReturn(Optional.of(invitation(200, trip, marko)));

        service.accept(200, 2);

        assertThat(anaMember.hasArchiveConsent()).isFalse();
    }

    @Test
    void archivedEventRejectsInvitationsAndAnswers() {
        trip.archive();
        when(invitations.findById(200L)).thenReturn(Optional.of(invitation(200, trip, marko)));

        assertStatus(() -> service.invite(10, 1, "iva"), HttpStatus.CONFLICT);
        assertStatus(() -> service.accept(200, 2), HttpStatus.CONFLICT);
    }

    // Scenarij "Pozivnica": Ana poziva Petra, koji joj nije prijatelj
    @Test
    void onlyFriendsCanBeInvited() {
        assertStatus(() -> service.invite(10, 1, "petar"), HttpStatus.FORBIDDEN);
        verify(invitations, never()).save(any());
    }

    @Test
    void declinedUserCanBeInvitedAgain() {
        when(invitations.existsByEventIdAndInviteeIdAndStatus(10L, 3L, Invitation.Status.PENDING)).thenReturn(false);

        assertThat(service.invite(10, 1, "iva").getStatus()).isEqualTo(Invitation.Status.PENDING);
    }

    @Test
    void pendingInvitationCannotBeDuplicated() {
        when(invitations.existsByEventIdAndInviteeIdAndStatus(10L, 2L, Invitation.Status.PENDING)).thenReturn(true);

        assertStatus(() -> service.invite(10, 1, "marko"), HttpStatus.CONFLICT);
    }

    @Test
    void existingMemberCannotBeInvited() {
        when(members.existsByEventIdAndUserId(10L, 2L)).thenReturn(true);

        assertStatus(() -> service.invite(10, 1, "marko"), HttpStatus.CONFLICT);
    }

    @Test
    void unknownUserCannotBeInvited() {
        when(users.findByUsername("niko")).thenReturn(Optional.empty());

        assertStatus(() -> service.invite(10, 1, "niko"), HttpStatus.NOT_FOUND);
    }

    @Test
    void onlyCreatorInvites() {
        when(members.findByEventIdAndUserId(10L, 2L)).thenReturn(Optional.of(member(101, trip, marko)));

        assertStatus(() -> service.invite(10, 2, "iva"), HttpStatus.FORBIDDEN);
    }

    @Test
    void nonMemberCannotInvite() {
        assertStatus(() -> service.invite(10, 3, "marko"), HttpStatus.NOT_FOUND);
    }

    @Test
    void onlyInviteeCanAnswerInvitation() {
        when(invitations.findById(200L)).thenReturn(Optional.of(invitation(200, trip, marko)));

        assertStatus(() -> service.accept(200, 3), HttpStatus.NOT_FOUND);
        assertStatus(() -> service.decline(200, 3), HttpStatus.NOT_FOUND);
    }

    @Test
    void answeredInvitationCannotBeAnsweredAgain() {
        Invitation declined = invitation(200, trip, marko);
        declined.decline();
        when(invitations.findById(200L)).thenReturn(Optional.of(declined));

        assertStatus(() -> service.accept(200, 2), HttpStatus.NOT_FOUND);
    }

    static void assertStatus(Runnable action, HttpStatusCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(expected));
    }
}
