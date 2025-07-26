package com.aptpath.payflowapi.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                    .title("PayFlow API")
                    .description("API documentation for Admin registration and login")
                    .version("v1.0"));
    }
}

