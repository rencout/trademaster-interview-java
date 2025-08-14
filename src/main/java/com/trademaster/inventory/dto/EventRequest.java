package com.trademaster.inventory.dto;

import com.trademaster.inventory.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequest {

    @NotNull(message = "Event type is required")
    private EventType type;

    @NotBlank(message = "SKU is required")
    private String sku;

    private Integer quantity;

    private Integer delta;

    private String payload;
}
