package com.trademaster.inventory.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class MetricsResponse {

    private Long totalInventoryItems;
    private Long totalEvents;
    private Long pendingEvents;
    private Long processedEvents;
    private Long failedEvents;
    private Long totalBatchJobs;
}
