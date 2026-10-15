package com.coinis.splitter.repository;

import com.coinis.splitter.model.Invitation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    List<Invitation> findAllByInviteeIdAndStatusOrderByIdAsc(Long inviteeId, Invitation.Status status);

    List<Invitation> findAllByEventIdAndStatusOrderByIdAsc(Long eventId, Invitation.Status status);

    boolean existsByEventIdAndInviteeIdAndStatus(Long eventId, Long inviteeId, Invitation.Status status);
}
