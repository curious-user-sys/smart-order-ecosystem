package com.smartorder.order.service;

import com.smartorder.order.client.ProductClient;
import com.smartorder.order.client.ProductDto;
import com.smartorder.order.dto.OrderRequest;
import com.smartorder.order.dto.OrderResponse;
import com.smartorder.order.entity.Order;
import com.smartorder.order.entity.OrderStatus;
import com.smartorder.order.event.OrderCreatedEvent;
import com.smartorder.order.exception.ResourceNotFoundException;
import com.smartorder.order.kafka.OrderEventProducer;
import com.smartorder.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderEventProducer orderEventProducer;

    public OrderService(
            OrderRepository orderRepository,
            ProductClient productClient,
            OrderEventProducer orderEventProducer
    ) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.orderEventProducer = orderEventProducer;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        ProductDto product = productClient.getProductById(request.getProductId());
        productClient.decreaseStock(request.getProductId(), request.getQuantity());

        BigDecimal unitPrice = product.getPrice();
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = new Order(
                request.getProductId(),
                request.getQuantity(),
                unitPrice,
                totalPrice,
                OrderStatus.CREATED,
                Instant.now()
        );

        Order saved = orderRepository.save(order);

        orderEventProducer.publishOrderCreated(new OrderCreatedEvent(
                saved.getId(),
                saved.getProductId(),
                saved.getQuantity(),
                saved.getCreatedAt()
        ));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sipariş bulunamadı. id=" + id));
        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sipariş bulunamadı. id=" + id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Sipariş zaten iptal edilmiş. id=" + id);
        }

        productClient.increaseStock(order.getProductId(), order.getQuantity());

        order.setStatus(OrderStatus.CANCELLED);
        Order updated = orderRepository.save(order);
        return toResponse(updated);
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getProductId(),
                order.getQuantity(),
                order.getUnitPrice(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}