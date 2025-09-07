package com.questionmaster.api.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "Gamified Question API",
        description = "A gamified question API inspired by Duolingo and Trivia Track",
        version = "1.0.0",
        contact = Contact(
            name = "API Support",
            email = "support@questionmaster.com"
        )
    ),
    servers = [
        Server(
            description = "Local Development",
            url = "http://localhost:8080"
        ),
        Server(
            description = "Production",
            url = "https://api.questionmaster.com"
        )
    ]
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
class OpenApiConfig
