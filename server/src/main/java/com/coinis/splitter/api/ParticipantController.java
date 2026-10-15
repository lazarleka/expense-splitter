package com.coinis.splitter.api;

import com.coinis.splitter.api.dto.ParticipantRequest;
import com.coinis.splitter.api.dto.ParticipantResponse;
import com.coinis.splitter.model.Participant;
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
@RequestMapping("/api/participants")
public class ParticipantController {

    private final ParticipantRepository participants;

    public ParticipantController(ParticipantRepository participants) {
        this.participants = participants;
    }

    @GetMapping
    public List<ParticipantResponse> list() {
        return participants.findAllByOrderByIdAsc().stream()
                .map(p -> new ParticipantResponse(p.getId(), p.getName()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipantResponse add(@Valid @RequestBody ParticipantRequest request) {
        String name = request.name().trim();
        if (participants.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ucesnik vec postoji: " + name);
        }
        Participant saved = participants.save(new Participant(name));
        return new ParticipantResponse(saved.getId(), saved.getName());
    }
}
