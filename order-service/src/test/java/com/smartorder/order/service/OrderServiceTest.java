package com.smartorder.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartorder.order.client.ProductClient;
import com.smartorder.order.client.ProductDto;
import com.smartorder.order.dto.OrderRequest;
import com.smartorder.order.dto.OrderResponse;
import com.smartorder.order.entity.Order;
import com.smartorder.order.entity.OrderStatus;
import com.smartorder.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_shouldCalculateTotalPriceAndDecreaseStock() {
        OrderRequest request = new OrderRequest(1L, 2);
        ProductDto product = new ProductDto(1L, "Kablosuz Mouse", new BigDecimal("799.90"), 13);

        when(productClient.getProductById(1L)).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response.getId());
        assertEquals(1L, response.getProductId());
        assertEquals(2, response.getQuantity());
        assertEquals(new BigDecimal("799.90"), response.getUnitPrice());
        assertEquals(new BigDecimal("1599.80"), response.getTotalPrice());
        assertEquals(OrderStatus.CREATED, response.getStatus());

        verify(productClient, times(1)).getProductById(1L);
        verify(productClient, times(1)).decreaseStock(1L, 2);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void cancelOrder_shouldRestoreStockAndMarkAsCancelled() {
        Order existingOrder = new Order(
                1L,
                2,
                new BigDecimal("799.90"),
                new BigDecimal("1599.80"),
                OrderStatus.CREATED,
                Instant.now()
        );
        existingOrder.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        verify(productClient, times(1)).increaseStock(1L, 2);
        verify(orderRepository, times(1)).save(existingOrder);
    }

    @Test
    void cancelOrder_whenAlreadyCancelled_shouldThrowExceptionAndNotIncreaseStock() {
        Order cancelledOrder = new Order(
                1L,
                2,
                new BigDecimal("799.90"),
                new BigDecimal("1599.80"),
                OrderStatus.CANCELLED,
                Instant.now()
        );
        cancelledOrder.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(cancelledOrder));

        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(1L));

        verify(productClient, never()).increaseStock(any(), any(Integer.class));
        verify(orderRepository, never()).save(any());
    }
}