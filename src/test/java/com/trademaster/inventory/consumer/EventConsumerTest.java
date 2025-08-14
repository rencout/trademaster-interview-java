package com.trademaster.inventory.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trademaster.inventory.domain.Event;
import com.trademaster.inventory.dto.EventRequest;
import com.trademaster.inventory.enums.EventType;
import com.trademaster.inventory.repository.EventRepository;
import com.trademaster.inventory.service.EventProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventConsumerTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventProcessingService eventProcessingService;

    @Mock
    private ObjectMapper objectMapper;

    private EventConsumer eventConsumer;

    @BeforeEach
    void setUp() {
        eventConsumer = new EventConsumer(eventRepository, eventProcessingService, objectMapper);
    }

    @Test
    void shouldSkipProcessing_whenHashAlreadyExists() throws Exception {
        // Given
        String rawMessage = "{\"type\":\"ORDER_PLACED\",\"sku\":\"PRODUCT-123\"}";
        Message message = new Message(rawMessage.getBytes(StandardCharsets.UTF_8));
        
        Event existingEvent = Event.builder()
                .id(1L)
                .hash("existing-hash")
                .build();

        when(eventRepository.findByHash(any())).thenReturn(Optional.of(existingEvent));

        // When
        eventConsumer.handleEvent(message, 1L);

        // Then
        verify(eventRepository, never()).save(any());
        verify(eventProcessingService, never()).processEvent(any());
    }

    @Test
    void shouldProcessEvent_whenHashIsNew() throws Exception {
        // Given
        String rawMessage = "{\"type\":\"ORDER_PLACED\",\"sku\":\"PRODUCT-123\"}";
        Message message = new Message(rawMessage.getBytes(StandardCharsets.UTF_8));
        
        EventRequest eventRequest = EventRequest.builder()
                .type(EventType.ORDER_PLACED)
                .sku("PRODUCT-123")
                .build();

        when(eventRepository.findByHash(any())).thenReturn(Optional.empty());
        when(objectMapper.readValue(rawMessage, EventRequest.class)).thenReturn(eventRequest);
        when(eventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        eventConsumer.handleEvent(message, 1L);

        // Then
        verify(eventRepository).save(any());
        verify(eventProcessingService).processEvent(any());
    }

    @Test
    void shouldHandleProcessingFailure_gracefully() throws Exception {
        // Given
        String rawMessage = "{\"type\":\"ORDER_PLACED\",\"sku\":\"PRODUCT-123\"}";
        Message message = new Message(rawMessage.getBytes(StandardCharsets.UTF_8));
        
        when(eventRepository.findByHash(any())).thenReturn(Optional.empty());
        when(objectMapper.readValue(rawMessage, EventRequest.class)).thenThrow(new RuntimeException("JSON parsing failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> eventConsumer.handleEvent(message, 1L));
    }
}
