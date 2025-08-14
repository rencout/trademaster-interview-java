package com.trademaster.inventory.controller;

import com.trademaster.inventory.dto.EventRequest;
import com.trademaster.inventory.dto.MetricsResponse;
import com.trademaster.inventory.enums.EventStatus;
import com.trademaster.inventory.repository.BatchJobRepository;
import com.trademaster.inventory.repository.EventRepository;
import com.trademaster.inventory.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final RabbitTemplate rabbitTemplate;
    private final EventRepository eventRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final BatchJobRepository batchJobRepository;

    @PostMapping
    public ResponseEntity<String> publishEvent(@Valid @RequestBody EventRequest eventRequest) {
        log.info("Publishing event: {} for SKU: {}", eventRequest.getType(), eventRequest.getSku());
        
        rabbitTemplate.convertAndSend("orders.events", eventRequest);
        
        return ResponseEntity.ok("Event published successfully");
    }

    @GetMapping("/metrics")
    public ResponseEntity<MetricsResponse> getMetrics() {
        MetricsResponse metrics = MetricsResponse.builder()
                .totalInventoryItems(inventoryItemRepository.count())
                .totalEvents(eventRepository.count())
                .pendingEvents(eventRepository.countByStatusIn(
                        java.util.List.of(EventStatus.RECEIVED, EventStatus.RETRY)))
                .processedEvents(eventRepository.countByStatus(EventStatus.PROCESSED))
                .failedEvents(eventRepository.countByStatus(EventStatus.DLQ))
                .totalBatchJobs(batchJobRepository.count())
                .build();

        return ResponseEntity.ok(metrics);
    }
}
