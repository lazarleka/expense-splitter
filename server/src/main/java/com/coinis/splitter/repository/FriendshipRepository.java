package com.coinis.splitter.repository;

import com.coinis.splitter.model.Friendship;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /** Zahtjevi sa datim statusom u kojima korisnik ucestvuje, u bilo kom smjeru. */
    @Query("""
            select f from Friendship f
            where f.status = :status and (f.requester.id = :userId or f.addressee.id = :userId)
            order by f.id""")
    List<Friendship> findAllInvolving(@Param("userId") Long userId, @Param("status") Friendship.Status status);

    /** Da li izmedju dva korisnika postoji zahtjev sa nekim od statusa, u bilo kom smjeru. */
    @Query("""
            select count(f) > 0 from Friendship f
            where f.status in :statuses
              and ((f.requester.id = :a and f.addressee.id = :b) or (f.requester.id = :b and f.addressee.id = :a))""")
    boolean existsBetween(@Param("a") Long a, @Param("b") Long b,
            @Param("statuses") Collection<Friendship.Status> statuses);

    List<Friendship> findAllByAddresseeIdAndStatusOrderByIdAsc(Long addresseeId, Friendship.Status status);

    List<Friendship> findAllByRequesterIdAndStatusOrderByIdAsc(Long requesterId, Friendship.Status status);
}
