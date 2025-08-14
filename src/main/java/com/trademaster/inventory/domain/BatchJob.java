package com.trademaster.inventory.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "batch_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "chunk_size", nullable = false)
    private Integer chunkSize;

    @Column(name = "total_processed", nullable = false)
    private Integer totalProcessed;

    @Column(name = "total_failed", nullable = false)
    private Integer totalFailed;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
