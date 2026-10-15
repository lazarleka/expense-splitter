package com.coinis.splitter.api;

import com.coinis.splitter.api.dto.InvitationResponse;
import com.coinis.splitter.model.Invitation;
import com.coinis.splitter.security.AuthUser;
import com.coinis.splitter.service.InvitationService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Pozivnice prijavljenog korisnika. Pozivanje je POST /api/events/{id}/invitations. */
@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @GetMapping
    public List<InvitationResponse> pending(@AuthenticationPrincipal AuthUser user) {
        return invitationService.pending(user.id()).stream()
                .map(InvitationController::toResponse)
                .toList();
    }

    @PostMapping("/{id}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void accept(@AuthenticationPrincipal AuthUser user, @PathVariable long id) {
        invitationService.accept(id, user.id());
    }

    @PostMapping("/{id}/decline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void decline(@AuthenticationPrincipal AuthUser user, @PathVariable long id) {
        invitationService.decline(id, user.id());
    }

    static InvitationResponse toResponse(Invitation i) {
        return new InvitationResponse(i.getId(), i.getEvent().getId(), i.getEvent().getName(),
                i.getEvent().getCreator().getUsername(), i.getInvitee().getUsername());
    }
}
