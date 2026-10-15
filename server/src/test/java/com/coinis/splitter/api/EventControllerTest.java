package com.coinis.splitter.api;

import static com.coinis.splitter.TestData.event;
import static com.coinis.splitter.TestData.member;
import static com.coinis.splitter.TestData.user;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coinis.splitter.model.Event;
import com.coinis.splitter.model.User;
import com.coinis.splitter.repository.UserRepository;
import com.coinis.splitter.security.AuthUser;
import com.coinis.splitter.security.SecurityConfig;
import com.coinis.splitter.service.EventService;
import com.coinis.splitter.service.InvitationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest({EventController.class, InvitationController.class})
@Import(SecurityConfig.class)
class EventControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private InvitationService invitationService;

    @MockitoBean
    private UserRepository users;

    private final AuthUser iva = new AuthUser(3, "iva", "hash");

    // Scenarij "Pristup": bez prijave nista osim registracije i prijave
    @Test
    void withoutLoginEverythingIsUnauthorized() throws Exception {
        mvc.perform(get("/api/events")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/events/10")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/invitations")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/events").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Izlet\"}"))
                .andExpect(status().isUnauthorized());
    }

    // Scenarij "Pristup": korisnik koji nije clan ne vidi dogadjaj
    @Test
    void nonMemberGetsNotFound() throws Exception {
        when(eventService.requireMember(10, 3)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mvc.perform(get("/api/events/10").with(user(iva))).andExpect(status().isNotFound());
    }

    // Scenarij "Validacija unosa": prazan naziv dogadjaja
    @Test
    void emptyEventNameIsRejected() throws Exception {
        mvc.perform(post("/api/events").with(user(iva)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"  \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listSplitsActiveAndArchived() throws Exception {
        User ana = user(1, "ana");
        Event active = event(10, "Izlet", ana);
        Event archived = event(11, "Rodjendan", ana);
        archived.archive();
        when(eventService.eventsOf(3)).thenReturn(List.of(active, archived));

        mvc.perform(get("/api/events").with(user(iva)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active[0].name").value("Izlet"))
                .andExpect(jsonPath("$.archived[0].name").value("Rodjendan"));
    }

    @Test
    void detailListsMembersInJoinOrder() throws Exception {
        User ana = user(1, "ana");
        User ivaUser = user(3, "iva");
        Event trip = event(10, "Izlet", ana);
        when(eventService.requireMember(10, 3)).thenReturn(member(101, trip, ivaUser));
        when(eventService.members(10)).thenReturn(List.of(member(100, trip, ana), member(101, trip, ivaUser)));

        mvc.perform(get("/api/events/10").with(user(iva)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members[0].username").value("ana"))
                .andExpect(jsonPath("$.members[1].username").value("iva"));
    }
}
