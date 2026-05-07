package com.promotrack.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupermarketCreateRequest(
        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must be at most 120 characters")
        String name,

        @Size(max = 500, message = "description must be at most 500 characters")
        String description,

        @Size(max = 255, message = "website must be at most 255 characters")
        String website,

        @NotBlank(message = "country is required")
        @Size(max = 80, message = "country must be at most 80 characters")
        String country
) {
}
