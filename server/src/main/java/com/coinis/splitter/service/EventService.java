package com.coinis.splitter.service;

import com.coinis.splitter.model.Event;
import com.coinis.splitter.model.EventMember;
import com.coinis.splitter.model.User;
import com.coinis.splitter.repository.EventMemberRepository;
import com.coinis.splitter.repository.EventRepository;
import com.coinis.splitter.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EventService {

    private final EventRepository events;
    private final EventMemberRepository members;
    private final UserRepository users;

    public EventService(EventRepository events, EventMemberRepository members, UserRepository users) {
        this.events = events;
        this.members = members;
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

    static void requireActive(Event event) {
        if (event.isArchived()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Dogadjaj je arhiviran");
        }
    }
}
