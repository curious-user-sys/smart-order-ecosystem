package com.smartorder.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartOrder - Order Service API")
                        .version("v1")
                        .description("Sipariş oluşturma, listeleme ve iptal/stok iadesi REST API dokümantasyonu"));
    }
}