package com.coinis.splitter.api;

import com.coinis.splitter.api.dto.ExpenseRequest;
import com.coinis.splitter.api.dto.ExpenseResponse;
import com.coinis.splitter.model.Expense;
import com.coinis.splitter.model.Participant;
import com.coinis.splitter.repository.ExpenseRepository;
import com.coinis.splitter.repository.ParticipantRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseRepository expenses;
    private final ParticipantRepository participants;

    public ExpenseController(ExpenseRepository expenses, ParticipantRepository participants) {
        this.expenses = expenses;
        this.participants = participants;
    }

    @GetMapping
    public List<ExpenseResponse> list() {
        return expenses.findAllByOrderByIdAsc().stream()
                .map(ExpenseController::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse add(@Valid @RequestBody ExpenseRequest request) {
        Participant payer = participants.findById(request.payerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Platilac nije ucesnik grupe"));
        Expense saved = expenses.save(new Expense(request.title().trim(), request.amountCents(), payer));
        return toResponse(saved);
    }

    private static ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getTitle(), e.getAmountCents(),
                e.getPayer().getId(), e.getPayer().getName());
    }
}
