package com.srishti.surgepricing.repository;

import com.srishti.surgepricing.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurgeRepository extends JpaRepository<Event, Long> {
}