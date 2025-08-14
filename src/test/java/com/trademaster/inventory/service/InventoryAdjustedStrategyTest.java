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
class InventoryAdjustedStrategyTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    private InventoryAdjustedStrategy inventoryAdjustedStrategy;

    @BeforeEach
    void setUp() {
        inventoryAdjustedStrategy = new InventoryAdjustedStrategy(inventoryItemRepository);
    }

    @Test
    void shouldApplyPositiveDelta_whenInventoryAdjusted() {
        // Given
        EventRequest eventRequest = EventRequest.builder()
                .type(EventType.INVENTORY_ADJUSTED)
                .sku("PRODUCT-123")
                .delta(5)
                .build();

        // When
        inventoryAdjustedStrategy.execute(eventRequest);

        // Then
        verify(inventoryItemRepository).adjustQuantityBySku("PRODUCT-123", 5);
    }

    @Test
    void shouldApplyNegativeDelta_whenInventoryAdjusted() {
        // Given
        EventRequest eventRequest = EventRequest.builder()
                .type(EventType.INVENTORY_ADJUSTED)
                .sku("PRODUCT-123")
                .delta(-3)
                .build();

        // When
        inventoryAdjustedStrategy.execute(eventRequest);

        // Then
        verify(inventoryItemRepository).adjustQuantityBySku("PRODUCT-123", -3);
    }

    @Test
    void shouldThrowException_whenDeltaIsMissing() {
        // Given
        EventRequest eventRequest = EventRequest.builder()
                .type(EventType.INVENTORY_ADJUSTED)
                .sku("PRODUCT-123")
                .build();

        // When & Then
        assertThrows(RuntimeException.class, () -> inventoryAdjustedStrategy.execute(eventRequest));
    }
}
