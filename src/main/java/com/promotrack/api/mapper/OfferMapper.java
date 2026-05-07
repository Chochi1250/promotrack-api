package com.promotrack.api.mapper;

import com.promotrack.api.domain.model.Offer;
import com.promotrack.api.domain.model.Supermarket;
import com.promotrack.api.dto.request.OfferCreateRequest;
import com.promotrack.api.dto.request.OfferUpdateRequest;
import com.promotrack.api.dto.response.OfferResponse;
import org.springframework.stereotype.Component;

@Component
public class OfferMapper {

    public Offer toModel(OfferCreateRequest request) {
        Offer offer = new Offer(
                request.title(),
                request.description(),
                request.category(),
                request.discountType(),
                request.startDate(),
                request.endDate(),
                request.source(),
                null
        );
        offer.setDiscountValue(request.discountValue());
        offer.setOriginalPrice(request.originalPrice());
        offer.setFinalPrice(request.finalPrice());
        return offer;
    }

    public Offer toModel(OfferUpdateRequest request) {
        Offer offer = new Offer(
                request.title(),
                request.description(),
                request.category(),
                request.discountType(),
                request.startDate(),
                request.endDate(),
                request.source(),
                null
        );
        offer.setDiscountValue(request.discountValue());
        offer.setOriginalPrice(request.originalPrice());
        offer.setFinalPrice(request.finalPrice());

        if (request.supermarketId() != null) {
            Supermarket supermarket = new Supermarket(null, null, null, null);
            supermarket.setId(request.supermarketId());
            offer.setSupermarket(supermarket);
        }

        return offer;
    }

    public OfferResponse toResponse(Offer offer) {
        Long supermarketId = offer.getSupermarket() != null ? offer.getSupermarket().getId() : null;
        return new OfferResponse(
                offer.getId(),
                offer.getTitle(),
                offer.getDescription(),
                offer.getCategory(),
                offer.getDiscountType(),
                offer.getDiscountValue(),
                offer.getOriginalPrice(),
                offer.getFinalPrice(),
                offer.getStartDate(),
                offer.getEndDate(),
                offer.getSource(),
                offer.isActive(),
                supermarketId,
                offer.getCreatedAt(),
                offer.getUpdatedAt()
        );
    }
}
