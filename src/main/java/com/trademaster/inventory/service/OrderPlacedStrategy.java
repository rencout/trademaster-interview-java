package com.trademaster.inventory.service;

import com.trademaster.inventory.dto.EventRequest;
import com.trademaster.inventory.enums.EventType;
import com.trademaster.inventory.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPlacedStrategy implements EventProcessingStrategy {

    private final InventoryItemRepository inventoryItemRepository;

    @Override
    public EventType supports() {
        return EventType.ORDER_PLACED;
    }

    @Override
    public void execute(EventRequest eventRequest) {
        log.info("Processing ORDER_PLACED event for SKU: {}", eventRequest.getSku());
        
        int quantity = eventRequest.getQuantity() != null ? eventRequest.getQuantity() : 1;
        int updatedRows = inventoryItemRepository.decrementQuantityIfAvailable(eventRequest.getSku(), quantity);
        
        if (updatedRows == 0) {
            throw new RuntimeException("Insufficient inventory for SKU: " + eventRequest.getSku());
        }
        
        log.info("Successfully decremented inventory for SKU: {} by quantity: {}", eventRequest.getSku(), quantity);
    }
}
