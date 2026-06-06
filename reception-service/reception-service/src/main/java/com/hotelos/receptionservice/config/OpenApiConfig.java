package com.hotelos.receptionservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "HotelOS - Reception & Booking Service API",
                version = "1.0",
                description = "Mehmonlarni ro'yxatga olish (Check-in/Check-out), xonalarni band qilish (Booking) va mijozlar hisob-kitoblarini boshqarish xizmati."
        )
)
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                        .servers(List.of(
                                new Server().url("/")
                        ));
        }
}
