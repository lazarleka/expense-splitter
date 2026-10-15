package com.coinis.splitter.repository;

import com.coinis.splitter.model.EventMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventMemberRepository extends JpaRepository<EventMember, Long> {

    // Red ulaska u dogadjaj; od njega zavise zaokruzivanje i redoslijed dugovanja
    List<EventMember> findAllByEventIdOrderByIdAsc(Long eventId);

    List<EventMember> findAllByUserIdOrderByIdAsc(Long userId);

    Optional<EventMember> findByEventIdAndUserId(Long eventId, Long userId);

    boolean existsByEventIdAndUserId(Long eventId, Long userId);
}
