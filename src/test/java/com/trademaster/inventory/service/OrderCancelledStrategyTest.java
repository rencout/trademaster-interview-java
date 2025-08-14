package com.trademaster.inventory.service;

import com.trademaster.inventory.dto.EventRequest;
import com.trademaster.inventory.enums.EventType;
import com.trademaster.inventory.repository.InventoryItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCancelledStrategyTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    private OrderCancelledStrategy orderCancelledStrategy;

    @BeforeEach
    void setUp() {
        orderCancelledStrategy = new OrderCancelledStrategy(inventoryItemRepository);
    }

    @Test
    void shouldIncrementQuantity_whenOrderCancelled() {
        // Given
        EventRequest eventRequest = EventRequest.builder()
                .type(EventType.ORDER_CANCELLED)
                .sku("PRODUCT-123")
                .quantity(3)
                .build();

        // When
        orderCancelledStrategy.execute(eventRequest);

        // Then
        verify(inventoryItemRepository).adjustQuantityBySku("PRODUCT-123", 3);
    }

    @Test
    void shouldUseDefaultQuantity_whenQuantityNotSpecified() {
        // Given
        EventRequest eventRequest = EventRequest.builder()
                .type(EventType.ORDER_CANCELLED)
                .sku("PRODUCT-123")
                .build();

        // When
        orderCancelledStrategy.execute(eventRequest);

        // Then
        verify(inventoryItemRepository).adjustQuantityBySku("PRODUCT-123", 1);
    }
}
