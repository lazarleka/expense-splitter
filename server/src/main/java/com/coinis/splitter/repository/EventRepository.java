package com.coinis.splitter.repository;

import com.coinis.splitter.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
