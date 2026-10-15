package com.coinis.splitter.repository;

import com.coinis.splitter.model.Participant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    boolean existsByName(String name);

    List<Participant> findAllByOrderByIdAsc();
}
