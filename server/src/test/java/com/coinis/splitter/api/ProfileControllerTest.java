package com.coinis.splitter.api;

import static com.coinis.splitter.TestData.user;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coinis.splitter.model.Avatar;
import com.coinis.splitter.model.User;
import com.coinis.splitter.repository.UserRepository;
import com.coinis.splitter.security.AuthUser;
import com.coinis.splitter.security.SecurityConfig;
import com.coinis.splitter.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository users;

    private final AuthUser ana = new AuthUser(1, "ana", "hash");

    // Scenarij "Avatar": Ana bira lisicu
    @Test
    void chosenAvatarIsSaved() throws Exception {
        User withFox = user(1, "ana");
        withFox.setAvatar(Avatar.FOX);
        when(userService.setAvatar(1, Avatar.FOX)).thenReturn(withFox);

        setAvatar("{\"avatar\": \"FOX\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatar").value("FOX"));
    }

    // Scenarij "Avatar": avatar koji nije u skupu se ne prihvata
    @Test
    void unknownAvatarIsRejected() throws Exception {
        setAvatar("{\"avatar\": \"DRAGON\"}").andExpect(status().isBadRequest());
        setAvatar("{}").andExpect(status().isBadRequest());
        verify(userService, never()).setAvatar(anyLong(), any());
    }

    @Test
    void withoutLoginIsUnauthorized() throws Exception {
        mvc.perform(put("/api/profile/avatar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"avatar\": \"FOX\"}"))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions setAvatar(String json) throws Exception {
        return mvc.perform(put("/api/profile/avatar").with(user(ana)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
