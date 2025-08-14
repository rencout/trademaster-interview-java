package com.trademaster.inventory.service;

import com.trademaster.inventory.domain.Event;
import com.trademaster.inventory.dto.EventRequest;
import com.trademaster.inventory.enums.EventStatus;
import com.trademaster.inventory.enums.EventType;
import com.trademaster.inventory.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventProcessingServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventStrategyFactory eventStrategyFactory;

    @Mock
    private OrderPlacedStrategy orderPlacedStrategy;

    private EventProcessingService eventProcessingService;

    @BeforeEach
    void setUp() throws Exception {
        eventProcessingService = new EventProcessingService(eventStrategyFactory, eventRepository);
        
        // Set maxRetries field using reflection since @Value doesn't work in unit tests
        Field maxRetriesField = EventProcessingService.class.getDeclaredField("maxRetries");
        maxRetriesField.setAccessible(true);
        maxRetriesField.set(eventProcessingService, 3);
    }

    @Test
    void shouldProcessEventSuccessfully() {
        // Given
        Event event = Event.builder()
                .id(1L)
                .type(EventType.ORDER_PLACED)
                .sku("TEST-SKU")
                .attempts(0)
                .build();

        when(eventStrategyFactory.get(EventType.ORDER_PLACED)).thenReturn(orderPlacedStrategy);
        doNothing().when(orderPlacedStrategy).execute(any(EventRequest.class));

        // When
        eventProcessingService.processEvent(event);

        // Then
        verify(eventRepository).updateStatus(event.getId(), EventStatus.PROCESSED);
    }

    @Test
    void shouldMarkEventForRetry_whenAttemptsBelowMax() {
        // Given
        Event event = Event.builder()
                .id(1L)
                .type(EventType.ORDER_PLACED)
                .sku("TEST-SKU")
                .attempts(0)
                .build();

        when(eventStrategyFactory.get(EventType.ORDER_PLACED)).thenReturn(orderPlacedStrategy);
        doThrow(new RuntimeException("Processing failed")).when(orderPlacedStrategy).execute(any(EventRequest.class));

        // When
        eventProcessingService.processEvent(event);

        // Then
        verify(eventRepository).updateStatusAndIncrementAttempts(event.getId(), EventStatus.RETRY);
    }

    @Test
    void shouldRouteToDlq_whenAttemptsAtOrAboveMax() {
        // Given
        Event event = Event.builder()
                .id(1L)
                .type(EventType.ORDER_PLACED)
                .sku("TEST-SKU")
                .attempts(3)
                .build();

        when(eventStrategyFactory.get(EventType.ORDER_PLACED)).thenReturn(orderPlacedStrategy);
        doThrow(new RuntimeException("Processing failed")).when(orderPlacedStrategy).execute(any(EventRequest.class));

        // When
        eventProcessingService.processEvent(event);

        // Then
        verify(eventRepository).updateStatus(event.getId(), EventStatus.DLQ);
    }
}
