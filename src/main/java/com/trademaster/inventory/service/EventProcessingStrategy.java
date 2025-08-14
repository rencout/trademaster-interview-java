package com.trademaster.inventory.service;

import com.trademaster.inventory.dto.EventRequest;
import com.trademaster.inventory.enums.EventType;

public interface EventProcessingStrategy {

    EventType supports();

    void execute(EventRequest eventRequest);
}
