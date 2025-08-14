package com.trademaster.inventory.repository;

import com.trademaster.inventory.domain.BatchJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchJobRepository extends JpaRepository<BatchJob, Long> {
}
