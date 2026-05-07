package com.promotrack.api.dto.response;

import com.promotrack.api.domain.enums.DiscountType;
import com.promotrack.api.domain.enums.OfferSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OfferResponse(
        Long id,
        String title,
        String description,
        String category,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal originalPrice,
        BigDecimal finalPrice,
        LocalDate startDate,
        LocalDate endDate,
        OfferSource source,
        boolean active,
        Long supermarketId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
