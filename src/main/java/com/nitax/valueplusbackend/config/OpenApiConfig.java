package com.nitax.valueplusbackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    servers = {
      @Server(url = "https://backend.valueplusagency.com/api/v1", description = "prod server"),
      @Server(url = "https://backend-dev.valueplusagency.com/api/v1", description = "Dev server"),
      @Server(url = "http://localhost:3030/api/v1", description = "Default Server local"),
    },
    info = @Info(title = "BetaCare API", version = "v1"))
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer")
public class OpenApiConfig {}
