package com.promotrack.api.dto.response;

import java.time.LocalDateTime;

public record SupermarketResponse(
        Long id,
        String name,
        String description,
        String website,
        String country,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
