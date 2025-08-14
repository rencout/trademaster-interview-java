package com.trademaster.inventory.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDERS_EVENTS_QUEUE = "orders.events";
    public static final String ORDERS_EVENTS_DLQ = "orders.events.dlq";

    @Bean
    public Queue ordersEventsQueue() {
        return QueueBuilder.durable(ORDERS_EVENTS_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", ORDERS_EVENTS_DLQ)
                .build();
    }

    @Bean
    public Queue ordersEventsDLQ() {
        return QueueBuilder.durable(ORDERS_EVENTS_DLQ).build();
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
