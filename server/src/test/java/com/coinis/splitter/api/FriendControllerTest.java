package com.coinis.splitter.api;

import static com.coinis.splitter.TestData.friendship;
import static com.coinis.splitter.TestData.user;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coinis.splitter.model.User;
import com.coinis.splitter.repository.UserRepository;
import com.coinis.splitter.security.AuthUser;
import com.coinis.splitter.security.SecurityConfig;
import com.coinis.splitter.service.FriendService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FriendController.class)
@Import(SecurityConfig.class)
class FriendControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FriendService friendService;

    @MockitoBean
    private UserRepository users;

    private final AuthUser ana = new AuthUser(1, "ana", "hash");

    @Test
    void withoutLoginIsUnauthorized() throws Exception {
        mvc.perform(get("/api/friends")).andExpect(status().isUnauthorized());
    }

    @Test
    void emptyUsernameIsRejected() throws Exception {
        mvc.perform(post("/api/friends/requests").with(user(ana)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"username\": \" \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listShowsFriendsAndPendingRequests() throws Exception {
        User anaUser = user(1, "ana");
        User marko = user(2, "marko");
        User iva = user(3, "iva");
        User petar = user(4, "petar");
        when(friendService.friends(1)).thenReturn(List.of(marko));
        when(friendService.incoming(1)).thenReturn(List.of(friendship(20, iva, anaUser)));
        when(friendService.outgoing(1)).thenReturn(List.of(friendship(21, anaUser, petar)));

        mvc.perform(get("/api/friends").with(user(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.friends[0].username").value("marko"))
                .andExpect(jsonPath("$.incoming[0].id").value(20))
                .andExpect(jsonPath("$.incoming[0].user.username").value("iva"))
                .andExpect(jsonPath("$.outgoing[0].user.username").value("petar"));
    }

    @Test
    void acceptIsPassedToServiceForLoggedInUser() throws Exception {
        mvc.perform(post("/api/friends/requests/20/accept").with(user(ana)).with(csrf()))
                .andExpect(status().isNoContent());
        verify(friendService).accept(20, 1);
    }
}
