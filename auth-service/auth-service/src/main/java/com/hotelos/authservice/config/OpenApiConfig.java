package com.hotelos.authservice.config;

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
                title = "HotelOS - Authentication & Authorization Service API",
                version = "1.0",
                description = "Foydalanuvchilarni ro'yxatdan o'tkazish, tizimga kirish (Login), JWT tokenlarni yaratish va huquqlarni tekshirish xizmati."
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