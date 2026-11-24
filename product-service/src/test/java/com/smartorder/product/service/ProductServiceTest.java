package com.smartorder.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartorder.product.dto.ProductResponse;
import com.smartorder.product.entity.Product;
import com.smartorder.product.exception.InsufficientStockException;
import com.smartorder.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;


@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void decreaseStock_whenStockIsSufficient_shouldDecreaseAndReturnUpdatedProduct() {
        Product product = new Product("Kablosuz Mouse", new BigDecimal("799.90"), 13);
        ReflectionTestUtils.setField(product, "id", 1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.decreaseStock(1L, 3);

        assertEquals(10, response.getStock());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void decreaseStock_whenStockIsInsufficient_shouldThrowConflictException() {
        Product product = new Product("Kablosuz Mouse", new BigDecimal("799.90"), 5);
        ReflectionTestUtils.setField(product, "id", 1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class, () -> productService.decreaseStock(1L, 10));

        assertEquals(5, product.getStock());
        verify(productRepository, never()).save(any());
    }

    @Test
    void increaseStock_shouldAddQuantityToExistingStock() {
        Product product = new Product("Kablosuz Mouse", new BigDecimal("799.90"), 11);
        ReflectionTestUtils.setField(product, "id", 1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.increaseStock(1L, 2);

        assertEquals(13, response.getStock());
        verify(productRepository, times(1)).save(product);
    }
}