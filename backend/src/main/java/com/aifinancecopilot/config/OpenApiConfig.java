package com.aifinancecopilot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI aiFinanceCopilotOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Finance Copilot API")
                        .version("0.0.1")
                        .description("REST API for personal finance tracking and AI-powered insights."));
    }
}

