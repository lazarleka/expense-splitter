package com.coinis.splitter.service;

import com.coinis.splitter.model.Event;
import com.coinis.splitter.model.EventMember;
import com.coinis.splitter.model.Invitation;
import com.coinis.splitter.model.User;
import com.coinis.splitter.repository.EventMemberRepository;
import com.coinis.splitter.repository.InvitationRepository;
import com.coinis.splitter.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InvitationService {

    private final InvitationRepository invitations;
    private final EventMemberRepository members;
    private final UserRepository users;
    private final EventService eventService;

    public InvitationService(InvitationRepository invitations, EventMemberRepository members,
            UserRepository users, EventService eventService) {
        this.invitations = invitations;
        this.members = members;
        this.users = users;
        this.eventService = eventService;
    }

    /** Samo kreator poziva. Ne moze se pozvati clan ni korisnik koji vec ima pozivnicu na cekanju. */
    @Transactional
    public Invitation invite(long eventId, long inviterId, String username) {
        Event event = eventService.requireMember(eventId, inviterId).getEvent();
        if (!event.getCreator().getId().equals(inviterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Samo kreator poziva clanove");
        }
        EventService.requireActive(event);

        User invitee = users.findByUsername(username.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik ne postoji"));
        if (members.existsByEventIdAndUserId(eventId, invitee.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Korisnik je vec clan");
        }
        if (invitations.existsByEventIdAndInviteeIdAndStatus(eventId, invitee.getId(), Invitation.Status.PENDING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pozivnica vec ceka odgovor");
        }
        return invitations.save(new Invitation(event, invitee));
    }

    public List<Invitation> pending(long userId) {
        return invitations.findAllByInviteeIdAndStatusOrderByIdAsc(userId, Invitation.Status.PENDING);
    }

    /** Prihvatanje pravi clanstvo (korisnik ulazi u red ulaska) i ponistava saglasnosti za arhiviranje. */
    @Transactional
    public EventMember accept(long invitationId, long userId) {
        Invitation invitation = requirePending(invitationId, userId);
        EventService.requireActive(invitation.getEvent());
        invitation.accept();
        eventService.resetArchiveConsents(eventService.members(invitation.getEvent().getId()));
        return members.save(new EventMember(invitation.getEvent(), invitation.getInvitee()));
    }

    @Transactional
    public void decline(long invitationId, long userId) {
        requirePending(invitationId, userId).decline();
    }

    // Tudja ili vec odgovorena pozivnica izgleda kao nepostojeca
    private Invitation requirePending(long invitationId, long userId) {
        return invitations.findById(invitationId)
                .filter(i -> i.getInvitee().getId().equals(userId))
                .filter(i -> i.getStatus() == Invitation.Status.PENDING)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pozivnica ne postoji"));
    }
}
