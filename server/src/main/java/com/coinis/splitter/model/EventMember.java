package com.coinis.splitter.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Clanstvo u dogadjaju. Red se pravi tek kad korisnik postane clan (kreator pri kreiranju,
 * ostali pri prihvatanju pozivnice), pa je id rastuce = red ulaska. Od tog reda zavise
 * zaokruzivanje udjela, redoslijed dugovanja i mjesta za stolom.
 */
@Entity
@Table(name = "event_members", uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "user_id"}))
public class EventMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected EventMember() {
    }

    public EventMember(Event event, User user) {
        this.event = event;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public User getUser() {
        return user;
    }
}
