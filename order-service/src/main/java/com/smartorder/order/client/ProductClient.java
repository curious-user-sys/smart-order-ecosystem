package com.smartorder.order.client;

import com.smartorder.order.exception.InsufficientStockException;
import com.smartorder.order.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import java.time.Duration;
import java.net.http.HttpClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;
@Component
public class ProductClient {

	private final RestClient restClient;

    public ProductClient(
            RestClient.Builder restClientBuilder,
            @Value("${product.service.url}") String productServiceUrl,
            @Value("${product.service.connect-timeout:2s}") Duration connectTimeout,
            @Value("${product.service.read-timeout:3s}") Duration readTimeout
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        this.restClient = restClientBuilder
                .baseUrl(productServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public ProductDto getProductById(Long productId) {
        try {
            return restClient.get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .body(ProductDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Ürün bulunamadı. id=" + productId);
        }
    }

    public ProductDto decreaseStock(Long productId, int quantity) {
        try {
            return restClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/products/{id}/decrease-stock")
                            .queryParam("quantity", quantity)
                            .build(productId))
                    .retrieve()
                    .body(ProductDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Ürün bulunamadı. id=" + productId);
        } catch (HttpClientErrorException.Conflict ex) {
            throw new InsufficientStockException(
                    "Sipariş için yeterli stok yok. productId=" + productId + ", quantity=" + quantity
            );
        }
    }
    
    public ProductDto increaseStock(Long productId, int quantity) {
        try {
            return restClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/products/{id}/increase-stock")
                            .queryParam("quantity", quantity)
                            .build(productId))
                    .retrieve()
                    .body(ProductDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Ürün bulunamadı. id=" + productId);
        }
    }
}