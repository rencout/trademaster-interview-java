package com.trademaster.inventory.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trademaster.inventory.domain.Event;
import com.trademaster.inventory.dto.EventRequest;
import com.trademaster.inventory.enums.EventStatus;
import com.trademaster.inventory.repository.EventRepository;
import com.trademaster.inventory.service.EventProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {

    private final EventRepository eventRepository;
    private final EventProcessingService eventProcessingService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "orders.events")
    public void handleEvent(Message message, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            String rawMessage = new String(message.getBody(), StandardCharsets.UTF_8);
            String hash = generateHash(rawMessage);

            // Check idempotency
            if (eventRepository.findByHash(hash).isPresent()) {
                log.info("Duplicate event detected, skipping. Hash: {}", hash);
                return;
            }

            // Parse and store event
            EventRequest eventRequest = objectMapper.readValue(rawMessage, EventRequest.class);
            
            Event event = Event.builder()
                    .type(eventRequest.getType())
                    .sku(eventRequest.getSku())
                    .payload(eventRequest.getPayload())
                    .status(EventStatus.RECEIVED)
                    .attempts(0)
                    .hash(hash)
                    .build();

            eventRepository.save(event);
            log.info("Event received and stored: {}", event.getId());

            // Process event
            eventProcessingService.processEvent(event);

        } catch (Exception e) {
            log.error("Failed to process message", e);
            // Message will be rejected and sent to DLQ
            throw new RuntimeException("Message processing failed", e);
        }
    }

    private String generateHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
