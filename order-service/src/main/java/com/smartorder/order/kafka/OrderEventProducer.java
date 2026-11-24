package com.smartorder.order.kafka;

import com.smartorder.order.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final String orderCreatedTopic;

    public OrderEventProducer(
            KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate,
            @Value("${app.kafka.topics.order-created:order-created-topic}") String orderCreatedTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderCreatedTopic = orderCreatedTopic;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        String key = String.valueOf(event.productId());
        log.info("OrderCreatedEvent gönderiliyor -> topic={}, key={}, event={}",
                orderCreatedTopic, key, event);

        kafkaTemplate.send(orderCreatedTopic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("OrderCreatedEvent gönderilemedi -> orderId={}, productId={}",
                                event.orderId(), event.productId(), ex);
                    } else if (result != null) {
                        log.info("OrderCreatedEvent gönderildi -> topic={}, partition={}, offset={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}