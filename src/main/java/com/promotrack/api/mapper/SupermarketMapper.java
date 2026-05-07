package com.promotrack.api.mapper;

import com.promotrack.api.domain.model.Supermarket;
import com.promotrack.api.dto.request.SupermarketCreateRequest;
import com.promotrack.api.dto.request.SupermarketUpdateRequest;
import com.promotrack.api.dto.response.SupermarketResponse;
import org.springframework.stereotype.Component;

@Component
public class SupermarketMapper {

    public Supermarket toModel(SupermarketCreateRequest request) {
        return new Supermarket(
                request.name(),
                request.description(),
                request.website(),
                request.country()
        );
    }

    public Supermarket toModel(SupermarketUpdateRequest request) {
        return new Supermarket(
                request.name(),
                request.description(),
                request.website(),
                request.country()
        );
    }

    public SupermarketResponse toResponse(Supermarket supermarket) {
        return new SupermarketResponse(
                supermarket.getId(),
                supermarket.getName(),
                supermarket.getDescription(),
                supermarket.getWebsite(),
                supermarket.getCountry(),
                supermarket.isActive(),
                supermarket.getCreatedAt(),
                supermarket.getUpdatedAt()
        );
    }
}
