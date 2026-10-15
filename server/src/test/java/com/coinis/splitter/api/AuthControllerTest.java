package com.coinis.splitter.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coinis.splitter.model.Avatar;
import com.coinis.splitter.model.User;
import com.coinis.splitter.repository.UserRepository;
import com.coinis.splitter.security.AuthUser;
import com.coinis.splitter.security.SecurityConfig;
import com.coinis.splitter.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository users;

    @Test
    void meWithoutLoginIsUnauthorized() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsLoggedInUserWithAvatar() throws Exception {
        User ana = withId(new User("ana", "hash"), 1L);
        ana.setAvatar(Avatar.FOX);
        when(userService.get(1)).thenReturn(ana);

        mvc.perform(get("/api/auth/me").with(user(new AuthUser(1, "ana", "hash"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ana"))
                .andExpect(jsonPath("$.avatar").value("FOX"));
    }

    @Test
    void registerRejectsEmptyUsername() throws Exception {
        register("{\"username\": \"  \", \"password\": \"tajna1234\"}").andExpect(status().isBadRequest());
    }

    @Test
    void registerRejectsShortPassword() throws Exception {
        register("{\"username\": \"ana\", \"password\": \"kratka\"}").andExpect(status().isBadRequest());
    }

    @Test
    void registerRejectsDuplicateUsername() throws Exception {
        when(userService.register("ana", "tajna1234"))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT));

        register("{\"username\": \"ana\", \"password\": \"tajna1234\"}").andExpect(status().isConflict());
    }

    @Test
    void registerLogsUserIn() throws Exception {
        User ana = withId(new User("ana", "hash"), 1L);
        when(userService.register("ana", "tajna1234")).thenReturn(ana);

        register("{\"username\": \"ana\", \"password\": \"tajna1234\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("ana"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        givenUser("ana", "tajna1234");

        login("{\"username\": \"ana\", \"password\": \"pogresna1\"}").andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithCorrectPasswordReturnsUser() throws Exception {
        givenUser("ana", "tajna1234");

        login("{\"username\": \"ana\", \"password\": \"tajna1234\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ana"));
    }

    @Test
    void postWithoutCsrfTokenIsForbidden() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"ana\", \"password\": \"tajna1234\"}"))
                .andExpect(status().isForbidden());
    }

    private void givenUser(String username, String password) {
        User user = withId(new User(username, new BCryptPasswordEncoder().encode(password)), 1L);
        when(userService.get(1)).thenReturn(user);
        when(users.findByUsername(any())).thenReturn(Optional.empty());
        when(users.findByUsername(username)).thenReturn(Optional.of(user));
    }

    // Id dodjeljuje baza, a ovi testovi rade bez nje
    private static User withId(User user, long id) {
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private ResultActions register(String json) throws Exception {
        return mvc.perform(post("/api/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions login(String json) throws Exception {
        return mvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
