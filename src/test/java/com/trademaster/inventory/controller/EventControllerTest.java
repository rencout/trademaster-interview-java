package com.trademaster.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trademaster.inventory.dto.EventRequest;
import com.trademaster.inventory.enums.EventType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @MockBean
    private com.trademaster.inventory.repository.EventRepository eventRepository;

    @MockBean
    private com.trademaster.inventory.repository.InventoryItemRepository inventoryItemRepository;

    @MockBean
    private com.trademaster.inventory.repository.BatchJobRepository batchJobRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldPublishEventSuccessfully() throws Exception {
        // Given
        EventRequest eventRequest = EventRequest.builder()
                .type(EventType.ORDER_PLACED)
                .sku("TEST-SKU")
                .build();

        // When & Then
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Event published successfully"));
    }
}
