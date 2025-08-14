package com.trademaster.inventory.service;

import com.trademaster.inventory.domain.BatchJob;
import com.trademaster.inventory.domain.Event;
import com.trademaster.inventory.repository.BatchJobRepository;
import com.trademaster.inventory.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchProcessingService {

    private final EventRepository eventRepository;
    private final BatchJobRepository batchJobRepository;
    private final EventProcessingService eventProcessingService;

    @Value("${app.chunk-size:100}")
    private Integer chunkSize;

    @Scheduled(fixedDelayString = "${app.retry-delay-ms:5000}")
    @Transactional
    public void processPendingEvents() {
        log.info("Starting batch processing with chunk size: {}", chunkSize);

        BatchJob batchJob = BatchJob.builder()
                .startedAt(LocalDateTime.now())
                .chunkSize(chunkSize)
                .totalProcessed(0)
                .totalFailed(0)
                .build();

        batchJobRepository.save(batchJob);

        try {
            Page<Event> eventsPage = eventRepository.findPendingEvents(PageRequest.of(0, chunkSize));
            
            for (Event event : eventsPage.getContent()) {
                try {
                    eventProcessingService.processEvent(event);
                    batchJob.setTotalProcessed(batchJob.getTotalProcessed() + 1);
                } catch (Exception e) {
                    log.error("Failed to process event in batch: {}", event.getId(), e);
                    batchJob.setTotalFailed(batchJob.getTotalFailed() + 1);
                }
            }

            log.info("Batch processing completed. Processed: {}, Failed: {}", 
                    batchJob.getTotalProcessed(), batchJob.getTotalFailed());

        } finally {
            batchJob.setFinishedAt(LocalDateTime.now());
            batchJobRepository.save(batchJob);
        }
    }
}
