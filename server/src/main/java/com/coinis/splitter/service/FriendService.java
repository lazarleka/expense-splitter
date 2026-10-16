package com.coinis.splitter.service;

import com.coinis.splitter.model.Friendship;
import com.coinis.splitter.model.User;
import com.coinis.splitter.repository.FriendshipRepository;
import com.coinis.splitter.repository.UserRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FriendService {

    static final int MIN_SEARCH = 2;

    private final FriendshipRepository friendships;
    private final UserRepository users;

    public FriendService(FriendshipRepository friendships, UserRepository users) {
        this.friendships = friendships;
        this.users = users;
    }

    /**
     * Ne moze se poslati zahtjev sebi, postojecem prijatelju ni kad vec postoji zahtjev na cekanju
     * (u bilo kom smjeru). Odbijen zahtjev ne smeta novom.
     */
    @Transactional
    public Friendship request(long userId, String username) {
        User target = users.findByUsername(username.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik ne postoji"));
        if (target.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ne mozete dodati sebe");
        }
        if (friendships.existsBetween(userId, target.getId(), List.of(Friendship.Status.ACCEPTED))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vec ste prijatelji");
        }
        if (friendships.existsBetween(userId, target.getId(), List.of(Friendship.Status.PENDING))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Zahtjev vec ceka odgovor");
        }
        User me = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Korisnik ne postoji"));
        return friendships.save(new Friendship(me, target));
    }

    /** Prijatelji po korisnickom imenu. */
    public List<User> friends(long userId) {
        return friendships.findAllInvolving(userId, Friendship.Status.ACCEPTED).stream()
                .map(f -> f.other(userId))
                .sorted(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<Friendship> incoming(long userId) {
        return friendships.findAllByAddresseeIdAndStatusOrderByIdAsc(userId, Friendship.Status.PENDING);
    }

    public List<Friendship> outgoing(long userId) {
        return friendships.findAllByRequesterIdAndStatusOrderByIdAsc(userId, Friendship.Status.PENDING);
    }

    @Transactional
    public void accept(long requestId, long userId) {
        requirePendingFor(requestId, userId).accept();
    }

    @Transactional
    public void decline(long requestId, long userId) {
        requirePendingFor(requestId, userId).decline();
    }

    /** Posiljalac povlaci zahtjev dok jos ceka odgovor; zahtjev se brise. */
    @Transactional
    public void cancel(long requestId, long userId) {
        Friendship request = friendships.findById(requestId)
                .filter(f -> f.getRequester().getId().equals(userId))
                .filter(f -> f.getStatus() == Friendship.Status.PENDING)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zahtjev ne postoji"));
        friendships.delete(request);
    }

    /**
     * Do 8 korisnika ciji naziv sadrzi upit (najmanje 2 znaka), sa odnosom prema korisniku koji trazi,
     * da klijent odmah ponudi pravu akciju.
     */
    public List<UserSearchResult> search(long userId, String query) {
        String part = query == null ? "" : query.trim();
        if (part.length() < MIN_SEARCH) {
            return List.of();
        }
        Map<Long, UserSearchResult.Relation> relations = new HashMap<>();
        Map<Long, Long> requestIds = new HashMap<>();
        for (Friendship f : friendships.findAllInvolving(userId, Friendship.Status.ACCEPTED)) {
            relations.put(f.other(userId).getId(), UserSearchResult.Relation.FRIEND);
        }
        for (Friendship f : friendships.findAllInvolving(userId, Friendship.Status.PENDING)) {
            long other = f.other(userId).getId();
            boolean sentByMe = f.getRequester().getId().equals(userId);
            relations.put(other, sentByMe ? UserSearchResult.Relation.OUTGOING : UserSearchResult.Relation.INCOMING);
            requestIds.put(other, f.getId());
        }
        return users.findTop8ByUsernameContainingIgnoreCaseAndIdNotOrderByUsernameAsc(part, userId).stream()
                .map(u -> new UserSearchResult(u,
                        relations.getOrDefault(u.getId(), UserSearchResult.Relation.NONE),
                        requestIds.get(u.getId())))
                .toList();
    }

    public boolean areFriends(long a, long b) {
        return friendships.existsBetween(a, b, List.of(Friendship.Status.ACCEPTED));
    }

    // Na zahtjev odgovara samo primalac; tudji ili vec odgovoren zahtjev izgleda kao nepostojeci
    private Friendship requirePendingFor(long requestId, long userId) {
        return friendships.findById(requestId)
                .filter(f -> f.getAddressee().getId().equals(userId))
                .filter(f -> f.getStatus() == Friendship.Status.PENDING)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zahtjev ne postoji"));
    }
}
