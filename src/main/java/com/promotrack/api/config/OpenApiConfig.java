package com.promotrack.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI promoTrackOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("PromoTrack API")
                        .description("API REST para gestionar ofertas de supermercados argentinos")
                        .version("0.0.1-SNAPSHOT"));
    }
}
