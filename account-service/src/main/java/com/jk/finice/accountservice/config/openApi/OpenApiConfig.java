package com.jk.finice.accountservice.config.openApi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI accountServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FinIce Account Service API")
                        .description("""
                                Account management REST API for FinIce Banking Application.
                                
                                **Features:**
                                - Create savings and current accounts
                                - View account details and balances
                                - Update account settings and limits
                                - Close accounts (soft delete)
                                - Account summary dashboard
                                
                                **Account Types:**
                                - **SAVINGS**: Interest-bearing, limited transactions, max 5 per user
                                - **CURRENT**: No interest, unlimited transactions, max 1 per user
                                
                                **Authentication:**
                                All endpoints require JWT authentication via API Gateway.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("FinIce Development Team")
                                .email("khamroevwork@gmail.com")
                                .url("https://github.com/LastCoderBoy/FinIce"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("API Gateway (Production)"),
                        new Server()
                                .url("http://localhost:8082")
                                .description("Account Service (Direct - Development Only)")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token obtained from auth-service /api/v1/auth/login endpoint")));
    }
}
