package com.smartorder.inventory.event;

import java.time.Instant;

public record OrderCreatedEvent(
        Long orderId,
        Long productId,
        int quantity,
        Instant createdAt
) {
}