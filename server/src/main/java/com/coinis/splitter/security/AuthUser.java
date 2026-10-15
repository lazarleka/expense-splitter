package com.coinis.splitter.security;

import com.coinis.splitter.model.User;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Prijavljeni korisnik u sesiji. Kontroleri ga dobijaju preko @AuthenticationPrincipal,
 * pa se id korisnika nikad ne uzima iz tijela zahtjeva.
 */
public record AuthUser(long id, String username, String passwordHash) implements UserDetails {

    public static AuthUser from(User user) {
        return new AuthUser(user.getId(), user.getUsername(), user.getPasswordHash());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // Hash ne smije zavrsiti u logu
    @Override
    public String toString() {
        return "AuthUser[id=" + id + ", username=" + username + "]";
    }
}
