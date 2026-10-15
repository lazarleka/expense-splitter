package com.coinis.splitter.api.dto;

import com.coinis.splitter.model.Avatar;
import com.coinis.splitter.model.User;

/** avatar je null dok ga korisnik ne izabere. */
public record UserResponse(long id, String username, Avatar avatar) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getAvatar());
    }
}
