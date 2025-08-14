package com.trademaster.inventory.service;

import com.trademaster.inventory.enums.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventStrategyFactory {

    private final Map<EventType, EventProcessingStrategy> strategies;

    public EventStrategyFactory(List<EventProcessingStrategy> strategyList) {
        this.strategies = new EnumMap<>(EventType.class);
        
        for (EventProcessingStrategy strategy : strategyList) {
            EventType supportedType = strategy.supports();
            strategies.put(supportedType, strategy);
            log.info("Registered strategy {} for event type {}", 
                    strategy.getClass().getSimpleName(), supportedType);
        }
    }

    public EventProcessingStrategy get(EventType eventType) {
        EventProcessingStrategy strategy = strategies.get(eventType);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for event type: " + eventType);
        }
        return strategy;
    }
}
