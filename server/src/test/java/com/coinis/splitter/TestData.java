package com.coinis.splitter;

import com.coinis.splitter.model.Event;
import com.coinis.splitter.model.EventMember;
import com.coinis.splitter.model.Expense;
import com.coinis.splitter.model.Friendship;
import com.coinis.splitter.model.Invitation;
import com.coinis.splitter.model.User;
import org.springframework.test.util.ReflectionTestUtils;

/** Entiteti sa id-jevima za testove bez baze (id inace dodjeljuje baza). */
public final class TestData {

    private TestData() {
    }

    public static User user(long id, String username) {
        return withId(new User(username, "hash"), id);
    }

    public static Event event(long id, String name, User creator) {
        return withId(new Event(name, creator), id);
    }

    public static EventMember member(long id, Event event, User user) {
        return withId(new EventMember(event, user), id);
    }

    public static Expense expense(long id, Event event, long amountCents, User payer, int memberCount) {
        return withId(new Expense(event, "Trosak " + id, amountCents, payer, memberCount), id);
    }

    public static Friendship friendship(long id, User requester, User addressee) {
        return withId(new Friendship(requester, addressee), id);
    }

    public static Invitation invitation(long id, Event event, User invitee) {
        return withId(new Invitation(event, invitee), id);
    }

    public static <T> T withId(T entity, long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
