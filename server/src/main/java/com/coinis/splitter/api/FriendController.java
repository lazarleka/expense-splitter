package com.coinis.splitter.api;

import com.coinis.splitter.api.dto.FriendRequest;
import com.coinis.splitter.api.dto.FriendRequestResponse;
import com.coinis.splitter.api.dto.FriendsResponse;
import com.coinis.splitter.api.dto.UserResponse;
import com.coinis.splitter.api.dto.UserSearchResponse;
import com.coinis.splitter.model.Friendship;
import com.coinis.splitter.model.User;
import com.coinis.splitter.security.AuthUser;
import com.coinis.splitter.service.FriendService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping
    public FriendsResponse list(@AuthenticationPrincipal AuthUser user) {
        return new FriendsResponse(
                friendService.friends(user.id()).stream().map(FriendController::toUser).toList(),
                friendService.incoming(user.id()).stream().map(f -> toRequest(f, f.getRequester())).toList(),
                friendService.outgoing(user.id()).stream().map(f -> toRequest(f, f.getAddressee())).toList());
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public FriendRequestResponse request(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody FriendRequest request) {
        Friendship sent = friendService.request(user.id(), request.username());
        return toRequest(sent, sent.getAddressee());
    }

    @GetMapping("/search")
    public List<UserSearchResponse> search(@AuthenticationPrincipal AuthUser user,
            @RequestParam(name = "q", defaultValue = "") String query) {
        return friendService.search(user.id(), query).stream()
                .map(r -> new UserSearchResponse(r.user().getId(), r.user().getUsername(), r.relation().name(),
                        r.requestId()))
                .toList();
    }

    @PostMapping("/requests/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@AuthenticationPrincipal AuthUser user, @PathVariable long id) {
        friendService.cancel(id, user.id());
    }

    @PostMapping("/requests/{id}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void accept(@AuthenticationPrincipal AuthUser user, @PathVariable long id) {
        friendService.accept(id, user.id());
    }

    @PostMapping("/requests/{id}/decline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void decline(@AuthenticationPrincipal AuthUser user, @PathVariable long id) {
        friendService.decline(id, user.id());
    }

    static UserResponse toUser(User u) {
        return new UserResponse(u.getId(), u.getUsername());
    }

    private static FriendRequestResponse toRequest(Friendship f, User other) {
        return new FriendRequestResponse(f.getId(), toUser(other));
    }
}
