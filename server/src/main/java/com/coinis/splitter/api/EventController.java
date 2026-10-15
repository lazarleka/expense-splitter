package com.coinis.splitter.api;

import com.coinis.splitter.api.dto.EventDetailResponse;
import com.coinis.splitter.api.dto.EventListResponse;
import com.coinis.splitter.api.dto.EventRequest;
import com.coinis.splitter.api.dto.EventSummaryResponse;
import com.coinis.splitter.api.dto.ExpenseRequest;
import com.coinis.splitter.api.dto.ExpenseResponse;
import com.coinis.splitter.api.dto.InvitationResponse;
import com.coinis.splitter.api.dto.InviteRequest;
import com.coinis.splitter.api.dto.MemberResponse;
import com.coinis.splitter.api.dto.TransferResponse;
import com.coinis.splitter.model.Event;
import com.coinis.splitter.model.EventMember;
import com.coinis.splitter.model.Expense;
import com.coinis.splitter.security.AuthUser;
import com.coinis.splitter.service.EventOverview;
import com.coinis.splitter.service.EventService;
import com.coinis.splitter.service.InvitationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final InvitationService invitationService;

    public EventController(EventService eventService, InvitationService invitationService) {
        this.eventService = eventService;
        this.invitationService = invitationService;
    }

    @GetMapping
    public EventListResponse list(@AuthenticationPrincipal AuthUser user) {
        List<Event> mine = eventService.eventsOf(user.id());
        return new EventListResponse(
                mine.stream().filter(e -> !e.isArchived()).map(EventController::toSummary).toList(),
                mine.stream().filter(Event::isArchived).map(EventController::toSummary).toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventSummaryResponse create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody EventRequest request) {
        return toSummary(eventService.create(user.id(), request.name()));
    }

    @GetMapping("/{id}")
    public EventDetailResponse detail(@AuthenticationPrincipal AuthUser user, @PathVariable long id) {
        EventOverview overview = eventService.overview(id, user.id());
        Event event = overview.event();
        List<EventMember> ordered = overview.members();

        List<MemberResponse> members = IntStream.range(0, ordered.size())
                .mapToObj(i -> toMember(ordered.get(i), overview.balances()[i]))
                .toList();
        Map<Long, String> names = ordered.stream()
                .collect(Collectors.toMap(m -> m.getUser().getId(), m -> m.getUser().getUsername()));
        List<TransferResponse> transfers = overview.transfers().stream()
                .map(t -> new TransferResponse(names.get(t.fromId()), names.get(t.toId()), t.amountCents()))
                .toList();

        return new EventDetailResponse(event.getId(), event.getName(), event.isArchived(),
                event.getCreator().getId(), overview.totalCents(), members,
                overview.expenses().stream().map(EventController::toExpense).toList(), transfers);
    }

    @PostMapping("/{id}/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse addExpense(@AuthenticationPrincipal AuthUser user, @PathVariable long id,
            @Valid @RequestBody ExpenseRequest request) {
        return toExpense(eventService.addExpense(id, user.id(), request.title(), request.amountCents(),
                request.payerId()));
    }

    @PostMapping("/{id}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    public InvitationResponse invite(@AuthenticationPrincipal AuthUser user, @PathVariable long id,
            @Valid @RequestBody InviteRequest request) {
        return InvitationController.toResponse(invitationService.invite(id, user.id(), request.username()));
    }

    private static EventSummaryResponse toSummary(Event e) {
        return new EventSummaryResponse(e.getId(), e.getName(), e.getCreator().getUsername());
    }

    private static MemberResponse toMember(EventMember m, long balanceCents) {
        return new MemberResponse(m.getUser().getId(), m.getUser().getUsername(), balanceCents);
    }

    private static ExpenseResponse toExpense(Expense e) {
        return new ExpenseResponse(e.getId(), e.getTitle(), e.getAmountCents(),
                e.getPayer().getId(), e.getPayer().getUsername());
    }
}
