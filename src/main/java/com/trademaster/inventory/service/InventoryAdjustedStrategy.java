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
public class InventoryAdjustedStrategy implements EventProcessingStrategy {

    private final InventoryItemRepository inventoryItemRepository;

    @Override
    public EventType supports() {
        return EventType.INVENTORY_ADJUSTED;
    }

    @Override
    public void execute(EventRequest eventRequest) {
        log.info("Processing INVENTORY_ADJUSTED event for SKU: {}", eventRequest.getSku());
        
        Integer delta = eventRequest.getDelta();
        if (delta == null) {
            throw new RuntimeException("Delta is required for INVENTORY_ADJUSTED event");
        }
        
        inventoryItemRepository.adjustQuantityBySku(eventRequest.getSku(), delta);
        
        log.info("Successfully adjusted inventory for SKU: {} by delta: {}", eventRequest.getSku(), delta);
    }
}
