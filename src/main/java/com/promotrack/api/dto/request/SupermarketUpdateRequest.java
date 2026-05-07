package com.promotrack.api.dto.request;

import jakarta.validation.constraints.Size;

public record SupermarketUpdateRequest(
        @Size(max = 120, message = "name must be at most 120 characters")
        String name,

        @Size(max = 500, message = "description must be at most 500 characters")
        String description,

        @Size(max = 255, message = "website must be at most 255 characters")
        String website,

        @Size(max = 80, message = "country must be at most 80 characters")
        String country
) {
}
