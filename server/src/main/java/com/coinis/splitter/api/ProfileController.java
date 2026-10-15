package com.coinis.splitter.api;

import com.coinis.splitter.api.dto.AvatarRequest;
import com.coinis.splitter.api.dto.UserResponse;
import com.coinis.splitter.security.AuthUser;
import com.coinis.splitter.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Profil prijavljenog korisnika. */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/avatar")
    public UserResponse setAvatar(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody AvatarRequest request) {
        return UserResponse.from(userService.setAvatar(user.id(), request.avatar()));
    }
}
