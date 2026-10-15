package com.coinis.splitter.api;

import com.coinis.splitter.api.dto.LoginRequest;
import com.coinis.splitter.api.dto.RegisterRequest;
import com.coinis.splitter.api.dto.UserResponse;
import com.coinis.splitter.model.User;
import com.coinis.splitter.security.AuthUser;
import com.coinis.splitter.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Registracija, prijava i trenutni korisnik. Odjava je POST /api/auth/logout (SecurityConfig).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextRepository;
    private final SecurityContextHolderStrategy contextHolder = SecurityContextHolder.getContextHolderStrategy();

    public AuthController(UserService userService, AuthenticationManager authenticationManager,
            SecurityContextRepository contextRepository) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.contextRepository = contextRepository;
    }

    // Klijent ga zove prije prvog POST-a da dobije cookie XSRF-TOKEN
    @GetMapping("/csrf")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void csrf(CsrfToken token) {
        token.getToken();
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        User registered = userService.register(request.username(), request.password());
        AuthUser user = AuthUser.from(registered);
        startSession(UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()),
                httpRequest, httpResponse);
        return UserResponse.from(registered);
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                    request.username().trim(), request.password()));
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Pogresno korisnicko ime ili lozinka");
        }
        startSession(authentication, httpRequest, httpResponse);
        return UserResponse.from(userService.get(((AuthUser) authentication.getPrincipal()).id()));
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthUser user) {
        // Iz baze, jer se avatar mijenja posle prijave, a AuthUser u sesiji ga ne prati
        return UserResponse.from(userService.get(user.id()));
    }

    private void startSession(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        // Nova sesija posle prijave, da stari JSESSIONID ne vazi (session fixation)
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        SecurityContext context = contextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        contextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
    }
}
