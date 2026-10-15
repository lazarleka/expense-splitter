package com.coinis.splitter.api;

import com.coinis.splitter.api.dto.TransferResponse;
import com.coinis.splitter.model.Participant;
import com.coinis.splitter.repository.ExpenseRepository;
import com.coinis.splitter.repository.ParticipantRepository;
import com.coinis.splitter.settlement.PaidAmount;
import com.coinis.splitter.settlement.SettlementCalculator;
import com.coinis.splitter.settlement.Transfer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    private final ParticipantRepository participants;
    private final ExpenseRepository expenses;

    public SettlementController(ParticipantRepository participants, ExpenseRepository expenses) {
        this.participants = participants;
        this.expenses = expenses;
    }

    @GetMapping
    public List<TransferResponse> settle() {
        List<Participant> people = participants.findAllByOrderByIdAsc();
        List<Long> ids = people.stream().map(Participant::getId).toList();
        Map<Long, String> names = people.stream()
                .collect(Collectors.toMap(Participant::getId, Participant::getName));

        List<PaidAmount> paid = expenses.findAllByOrderByIdAsc().stream()
                .map(e -> new PaidAmount(e.getAmountCents(), e.getPayer().getId()))
                .toList();

        return SettlementCalculator.calculate(ids, paid).stream()
                .map(t -> toResponse(t, names))
                .toList();
    }

    private static TransferResponse toResponse(Transfer t, Map<Long, String> names) {
        return new TransferResponse(names.get(t.fromId()), names.get(t.toId()), t.amountCents());
    }
}
