package com.trademaster.inventory.service;

import com.trademaster.inventory.enums.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventStrategyFactoryTest {

    @Mock
    private OrderPlacedStrategy orderPlacedStrategy;

    @Mock
    private OrderCancelledStrategy orderCancelledStrategy;

    @Mock
    private InventoryAdjustedStrategy inventoryAdjustedStrategy;

    private EventStrategyFactory eventStrategyFactory;

    @BeforeEach
    void setUp() {
        when(orderPlacedStrategy.supports()).thenReturn(EventType.ORDER_PLACED);
        when(orderCancelledStrategy.supports()).thenReturn(EventType.ORDER_CANCELLED);
        when(inventoryAdjustedStrategy.supports()).thenReturn(EventType.INVENTORY_ADJUSTED);

        eventStrategyFactory = new EventStrategyFactory(
                List.of(orderPlacedStrategy, orderCancelledStrategy, inventoryAdjustedStrategy));
    }

    @Test
    void shouldReturnCorrectStrategyForEachEventType() {
        // When & Then
        assertEquals(orderPlacedStrategy, eventStrategyFactory.get(EventType.ORDER_PLACED));
        assertEquals(orderCancelledStrategy, eventStrategyFactory.get(EventType.ORDER_CANCELLED));
        assertEquals(inventoryAdjustedStrategy, eventStrategyFactory.get(EventType.INVENTORY_ADJUSTED));
    }
}
