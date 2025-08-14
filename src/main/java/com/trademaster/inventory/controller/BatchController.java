package com.trademaster.inventory.controller;

import com.trademaster.inventory.domain.BatchJob;
import com.trademaster.inventory.repository.BatchJobRepository;
import com.trademaster.inventory.service.BatchProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/batches")
@RequiredArgsConstructor
@Slf4j
public class BatchController {

    private final BatchJobRepository batchJobRepository;
    private final BatchProcessingService batchProcessingService;

    @GetMapping
    public ResponseEntity<List<BatchJob>> listBatchJobs() {
        List<BatchJob> batchJobs = batchJobRepository.findAll();
        return ResponseEntity.ok(batchJobs);
    }

    @PostMapping("/trigger")
    public ResponseEntity<String> triggerBatchJob() {
        log.info("Manual batch job trigger requested");
        batchProcessingService.processPendingEvents();
        return ResponseEntity.ok("Batch job triggered successfully");
    }
}
