package com.trademaster.inventory.service;

import com.trademaster.inventory.dto.EventRequest;
import com.trademaster.inventory.enums.EventType;
import com.trademaster.inventory.repository.InventoryItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderPlacedStrategyTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    private OrderPlacedStrategy orderPlacedStrategy;

    @BeforeEach
    void setUp() {
        orderPlacedStrategy = new OrderPlacedStrategy(inventoryItemRepository);
    }

    @Test
    void shouldDecrementQuantity_whenOrderPlaced() {
        // Given
        EventRequest eventRequest = EventRequest.builder()
                .type(EventType.ORDER_PLACED)
                .sku("PRODUCT-123")
                .quantity(2)
                .build();

        when(inventoryItemRepository.decrementQuantityIfAvailable("PRODUCT-123", 2)).thenReturn(1);

        // When
        orderPlacedStrategy.execute(eventRequest);

        // Then
        verify(inventoryItemRepository).decrementQuantityIfAvailable("PRODUCT-123", 2);
    }

    @Test
    void shouldThrowException_whenInsufficientInventory() {
        // Given
        EventRequest eventRequest = EventRequest.builder()
                .type(EventType.ORDER_PLACED)
                .sku("PRODUCT-123")
                .quantity(2)
                .build();

        when(inventoryItemRepository.decrementQuantityIfAvailable("PRODUCT-123", 2)).thenReturn(0);

        // When & Then
        assertThrows(RuntimeException.class, () -> orderPlacedStrategy.execute(eventRequest));
    }

    @Test
    void shouldUseDefaultQuantity_whenQuantityNotSpecified() {
        // Given
        EventRequest eventRequest = EventRequest.builder()
                .type(EventType.ORDER_PLACED)
                .sku("PRODUCT-123")
                .build();

        when(inventoryItemRepository.decrementQuantityIfAvailable("PRODUCT-123", 1)).thenReturn(1);

        // When
        orderPlacedStrategy.execute(eventRequest);

        // Then
        verify(inventoryItemRepository).decrementQuantityIfAvailable("PRODUCT-123", 1);
    }
}
