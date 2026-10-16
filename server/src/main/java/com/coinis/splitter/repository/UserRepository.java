package com.coinis.splitter.repository;

import com.coinis.splitter.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    /** Pretraga po dijelu imena, bez samog korisnika koji trazi. */
    List<User> findTop8ByUsernameContainingIgnoreCaseAndIdNotOrderByUsernameAsc(String part, Long excludedId);

    /** Svi korisnici za pregled liste, osim prijavljenog korisnika. */
    List<User> findAllByIdNotOrderByUsernameAsc(Long excludedId);
}
