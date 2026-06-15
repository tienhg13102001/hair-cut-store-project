package com.haircut.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hairCutOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Hair Cut Manager API")
                .description("REST API quản lý lịch hẹn salon — Spring Boot 4 + PostgreSQL")
                .version("v1.0"));
    }
}
