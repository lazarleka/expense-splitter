package com.coinis.splitter.service;

import com.coinis.splitter.model.Avatar;
import com.coinis.splitter.model.User;
import com.coinis.splitter.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public User register(String username, String rawPassword) {
        String name = username.trim();
        if (users.existsByUsername(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Korisnicko ime je zauzeto: " + name);
        }
        try {
            return users.save(new User(name, encoder.encode(rawPassword)));
        } catch (DataIntegrityViolationException e) {
            // Dvije istovremene registracije istog imena: unique ogranicenje u bazi
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Korisnicko ime je zauzeto: " + name);
        }
    }

    public User get(long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Korisnik ne postoji"));
    }

    @Transactional
    public User setAvatar(long userId, Avatar avatar) {
        User user = get(userId);
        user.setAvatar(avatar);
        return user;
    }
}
