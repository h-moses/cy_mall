package com.ms.order.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitmqConfig {

    public static final String CANCEL_QUEUE_NAME = "CANCEL_QUEUE";

    public static final String CANCEL_EXCHANGE_NAME = "CANCEL_EXCHANGE";

    public static final String CANCEL_ROUTING_KEY = "CANCEL_ROUTING";

    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> argument = new HashMap<>();
        argument.put("x-delayed-type", "direct");
        return new CustomExchange(CANCEL_EXCHANGE_NAME, "x-delayed-message", true, false, argument);
    }

    @Bean
    public Queue cancelQueue() {
        return new Queue(CANCEL_QUEUE_NAME);
    }

    @Bean
    public Binding cancelQueueBinding(CustomExchange delayedExchange, Queue cancelQueue) {
        return BindingBuilder.bind(cancelQueue).to(delayedExchange).with(CANCEL_ROUTING_KEY).noargs();
    }

}
