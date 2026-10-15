package com.coinis.splitter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coinis.splitter.model.User;
import com.coinis.splitter.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class UserServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final UserService service = new UserService(users, encoder);

    @Test
    void registerStoresTrimmedNameAndHashedPassword() {
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = service.register("  ana ", "tajna1234");

        assertThat(saved.getUsername()).isEqualTo("ana");
        assertThat(saved.getPasswordHash()).isNotEqualTo("tajna1234");
        assertThat(encoder.matches("tajna1234", saved.getPasswordHash())).isTrue();
    }

    @Test
    void duplicateUsernameIsRejected() {
        when(users.existsByUsername("ana")).thenReturn(true);

        assertThatThrownBy(() -> service.register("ana", "tajna1234"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        verify(users, never()).save(any());
    }
}
