package com.coinis.splitter.service;

import static com.coinis.splitter.TestData.friendship;
import static com.coinis.splitter.TestData.user;
import static com.coinis.splitter.service.InvitationServiceTest.assertStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coinis.splitter.model.Friendship;
import com.coinis.splitter.model.User;
import com.coinis.splitter.repository.FriendshipRepository;
import com.coinis.splitter.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class FriendServiceTest {

    private static final List<Friendship.Status> ACCEPTED = List.of(Friendship.Status.ACCEPTED);
    private static final List<Friendship.Status> PENDING = List.of(Friendship.Status.PENDING);

    private final FriendshipRepository friendships = mock(FriendshipRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final FriendService service = new FriendService(friendships, users);

    private final User ana = user(1, "ana");
    private final User marko = user(2, "marko");
    private final User iva = user(3, "iva");

    @BeforeEach
    void usersExist() {
        for (User u : List.of(ana, marko, iva)) {
            when(users.findByUsername(u.getUsername())).thenReturn(Optional.of(u));
            when(users.findById(u.getId())).thenReturn(Optional.of(u));
        }
        when(friendships.save(any(Friendship.class))).thenAnswer(i -> i.getArgument(0));
    }

    // Scenarij "Prijatelji": Ana salje zahtjev Marku i Ivi; Marko prihvata, Iva odbija
    @Test
    void acceptedRequestMakesMutualFriendsAndDeclinedDoesNot() {
        Friendship toMarko = service.request(1, "marko");
        Friendship toIva = service.request(1, " iva ");
        assertThat(toMarko.getRequester()).isEqualTo(ana);
        assertThat(toMarko.getAddressee()).isEqualTo(marko);
        assertThat(toIva.getAddressee()).isEqualTo(iva);
        assertThat(toMarko.getStatus()).isEqualTo(Friendship.Status.PENDING);

        Friendship markoRequest = friendship(10, ana, marko);
        Friendship ivaRequest = friendship(11, ana, iva);
        when(friendships.findById(10L)).thenReturn(Optional.of(markoRequest));
        when(friendships.findById(11L)).thenReturn(Optional.of(ivaRequest));

        service.accept(10, 2);
        service.decline(11, 3);

        assertThat(markoRequest.getStatus()).isEqualTo(Friendship.Status.ACCEPTED);
        assertThat(ivaRequest.getStatus()).isEqualTo(Friendship.Status.DECLINED);

        // Lista prijatelja dolazi iz prihvacenih zahtjeva; svako vidi onog drugog
        when(friendships.findAllInvolving(1L, Friendship.Status.ACCEPTED)).thenReturn(List.of(markoRequest));
        when(friendships.findAllInvolving(2L, Friendship.Status.ACCEPTED)).thenReturn(List.of(markoRequest));
        assertThat(service.friends(1)).containsExactly(marko);
        assertThat(service.friends(2)).containsExactly(ana);
    }

    // Scenarij "Prijatelji": posle odbijanja zahtjev se moze poslati ponovo
    @Test
    void declinedRequestCanBeSentAgain() {
        assertThat(service.request(1, "iva").getStatus()).isEqualTo(Friendship.Status.PENDING);
    }

    // Scenarij "Prijatelji": Marko salje zahtjev Ani, a vec su prijatelji
    @Test
    void friendsCannotRequestAgain() {
        when(friendships.existsBetween(eq(2L), eq(1L), eq(ACCEPTED))).thenReturn(true);

        assertStatus(() -> service.request(2, "ana"), HttpStatus.CONFLICT);
        verify(friendships, never()).save(any());
    }

    // Scenarij "Prijatelji": zahtjev na cekanju u bilo kom smjeru blokira novi
    @Test
    void pendingRequestInEitherDirectionBlocksNewOne() {
        when(friendships.existsBetween(eq(1L), eq(3L), eq(PENDING))).thenReturn(true);

        assertStatus(() -> service.request(1, "iva"), HttpStatus.CONFLICT);
    }

    @Test
    void cannotRequestSelf() {
        assertStatus(() -> service.request(1, "ana"), HttpStatus.BAD_REQUEST);
    }

    @Test
    void unknownUserCannotBeRequested() {
        when(users.findByUsername("niko")).thenReturn(Optional.empty());

        assertStatus(() -> service.request(1, "niko"), HttpStatus.NOT_FOUND);
    }

    @Test
    void onlyAddresseeAnswersRequest() {
        when(friendships.findById(10L)).thenReturn(Optional.of(friendship(10, ana, marko)));

        assertStatus(() -> service.accept(10, 1), HttpStatus.NOT_FOUND);
        assertStatus(() -> service.decline(10, 3), HttpStatus.NOT_FOUND);
    }

    @Test
    void answeredRequestCannotBeAnsweredAgain() {
        Friendship accepted = friendship(10, ana, marko);
        accepted.accept();
        when(friendships.findById(10L)).thenReturn(Optional.of(accepted));

        assertStatus(() -> service.decline(10, 2), HttpStatus.NOT_FOUND);
    }

    @Test
    void friendsAreSortedByName() {
        Friendship withMarko = friendship(10, ana, marko);
        Friendship withIva = friendship(11, iva, ana);
        when(friendships.findAllInvolving(1L, Friendship.Status.ACCEPTED)).thenReturn(List.of(withMarko, withIva));

        assertThat(service.friends(1)).containsExactly(iva, marko);
    }
}
