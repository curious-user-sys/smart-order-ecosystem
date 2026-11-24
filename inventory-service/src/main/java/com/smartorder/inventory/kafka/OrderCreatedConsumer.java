package com.smartorder.inventory.kafka;

import com.smartorder.inventory.event.OrderCreatedEvent;
import com.smartorder.inventory.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedConsumer.class);

    private final InventoryService inventoryService;

    public OrderCreatedConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
        topics = "order-created-topic",
        groupId = "${spring.kafka.consumer.group-id:inventory-service-group}"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("OrderCreatedEvent alındı -> orderId={}, productId={}, quantity={}",
            event.orderId(), event.productId(), event.quantity());
        inventoryService.deductStockForOrder(event.orderId(), event.productId(), event.quantity());
    }
}