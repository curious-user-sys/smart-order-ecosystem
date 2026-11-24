package com.smartorder.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartorder.order.dto.OrderRequest;
import com.smartorder.order.dto.OrderResponse;
import com.smartorder.order.entity.OrderStatus;
import com.smartorder.order.exception.InsufficientStockException;
import com.smartorder.order.exception.ResourceNotFoundException;
import com.smartorder.order.service.OrderService;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrder_whenValidRequest_shouldReturn201Created() throws Exception {
        OrderRequest request = new OrderRequest(1L, 2);
        OrderResponse response = new OrderResponse(
                1L,
                1L,
                2,
                new BigDecimal("799.90"),
                new BigDecimal("1599.80"),
                OrderStatus.CREATED,
                Instant.parse("2026-11-23T14:00:00Z")
        );

        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.totalPrice").value(1599.80))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void createOrder_whenInvalidQuantity_shouldReturn400ProblemDetail() throws Exception {
        String invalidJson = "{\"productId\":1,\"quantity\":0}";

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.quantity").exists());
    }

    @Test
    void getOrderById_whenNotFound_shouldReturn404ProblemDetail() throws Exception {
        when(orderService.getOrderById(999L))
                .thenThrow(new ResourceNotFoundException("Sipariş bulunamadı. id=999"));

        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Sipariş bulunamadı. id=999"));
    }

    @Test
    void cancelOrder_whenInsufficientStockOrAlreadyCancelled_shouldReturn409ProblemDetail() throws Exception {
        when(orderService.cancelOrder(1L))
                .thenThrow(new InsufficientStockException("Stok durumu çakıştı"));

        mockMvc.perform(patch("/api/orders/1/cancel"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(409));
    }
}