package com.coinis.splitter.api.dto;

import java.util.List;

public record FriendsResponse(
        List<UserResponse> friends,
        List<FriendRequestResponse> incoming,
        List<FriendRequestResponse> outgoing) {
}
