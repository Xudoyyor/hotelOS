package com.hotelos.gatewayservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "HotelOS - API Gateway Tizimi",
                version = "1.0",
                description = "HotelOS tizimidagi barcha mikroservislar API hujjatlarini markazlashtirilgan holda boshqarish va yo'naltirish eshigi."
        )
)
public class OpenApiConfig {
}
