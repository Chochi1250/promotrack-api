package com.promotrack.api.dto.request;

import com.promotrack.api.domain.enums.DiscountType;
import com.promotrack.api.domain.enums.OfferSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OfferCreateRequest(
        @NotBlank(message = "title is required")
        @Size(max = 160, message = "title must be at most 160 characters")
        String title,

        @Size(max = 700, message = "description must be at most 700 characters")
        String description,

        @NotBlank(message = "category is required")
        @Size(max = 100, message = "category must be at most 100 characters")
        String category,

        @NotNull(message = "discountType is required")
        DiscountType discountType,

        @PositiveOrZero(message = "discountValue must be zero or positive")
        BigDecimal discountValue,

        @Positive(message = "originalPrice must be positive")
        BigDecimal originalPrice,

        @Positive(message = "finalPrice must be positive")
        BigDecimal finalPrice,

        @NotNull(message = "startDate is required")
        LocalDate startDate,

        @NotNull(message = "endDate is required")
        LocalDate endDate,

        @NotNull(message = "source is required")
        OfferSource source,

        @NotNull(message = "supermarketId is required")
        @Positive(message = "supermarketId must be positive")
        Long supermarketId
) {
}
