package com.trademaster.inventory.service;

import com.trademaster.inventory.domain.Event;
import com.trademaster.inventory.dto.EventRequest;
import com.trademaster.inventory.enums.EventStatus;
import com.trademaster.inventory.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventProcessingService {

    private final EventStrategyFactory eventStrategyFactory;
    private final EventRepository eventRepository;

    @Value("${app.max-retries:3}")
    private Integer maxRetries;

    @Transactional
    public void processEvent(Event event) {
        log.info("Processing event: {} for SKU: {}", event.getType(), event.getSku());

        try {
            // Convert Event to EventRequest for strategy execution
            EventRequest eventRequest = EventRequest.builder()
                    .type(event.getType())
                    .sku(event.getSku())
                    .payload(event.getPayload())
                    .build();

            // Use factory to get strategy and execute
            eventStrategyFactory.get(event.getType()).execute(eventRequest);
            
            eventRepository.updateStatus(event.getId(), EventStatus.PROCESSED);
            log.info("Event processed successfully: {}", event.getId());
            
        } catch (Exception e) {
            log.error("Failed to process event: {}", event.getId(), e);
            handleProcessingFailure(event);
        }
    }

    private void handleProcessingFailure(Event event) {
        if (event.getAttempts() < maxRetries) {
            eventRepository.updateStatusAndIncrementAttempts(event.getId(), EventStatus.RETRY);
            log.info("Event marked for retry: {} (attempts: {})", event.getId(), event.getAttempts() + 1);
        } else {
            eventRepository.updateStatus(event.getId(), EventStatus.DLQ);
            log.warn("Event sent to DLQ: {} (max attempts reached)", event.getId());
        }
    }
}
