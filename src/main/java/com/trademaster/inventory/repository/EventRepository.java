package com.trademaster.inventory.repository;

import com.trademaster.inventory.domain.Event;
import com.trademaster.inventory.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByHash(String hash);

    @Query("SELECT e FROM Event e WHERE e.status IN ('RECEIVED', 'RETRY') ORDER BY e.createdAt ASC")
    Page<Event> findPendingEvents(Pageable pageable);

    @Modifying
    @Query("UPDATE Event e SET e.status = :status, e.attempts = e.attempts + 1 WHERE e.id = :id")
    void updateStatusAndIncrementAttempts(@Param("id") Long id, @Param("status") EventStatus status);

    @Modifying
    @Query("UPDATE Event e SET e.status = :status WHERE e.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") EventStatus status);

    long countByStatus(EventStatus status);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.status IN :statuses")
    long countByStatusIn(@Param("statuses") java.util.List<EventStatus> statuses);
}
