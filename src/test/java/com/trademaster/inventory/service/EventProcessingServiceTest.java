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
    void setUp() {
        eventProcessingService = new EventProcessingService(eventStrategyFactory, eventRepository);
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
        verify(eventStrategyFactory).get(EventType.ORDER_PLACED);
        verify(orderPlacedStrategy).execute(any(EventRequest.class));
        verify(eventRepository).updateStatus(event.getId(), EventStatus.PROCESSED);
        verify(eventRepository, never()).updateStatusAndIncrementAttempts(any(), any());
    }

    @Test
    void shouldMarkEventForRetryOnFailure() {
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
        verify(eventRepository, never()).updateStatus(eq(event.getId()), eq(EventStatus.PROCESSED));
    }
}
