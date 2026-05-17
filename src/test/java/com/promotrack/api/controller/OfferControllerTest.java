package com.promotrack.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promotrack.api.domain.enums.DiscountType;
import com.promotrack.api.domain.enums.OfferSource;
import com.promotrack.api.domain.model.Offer;
import com.promotrack.api.domain.model.Supermarket;
import com.promotrack.api.exception.GlobalExceptionHandler;
import com.promotrack.api.exception.InvalidDateRangeException;
import com.promotrack.api.mapper.OfferMapper;
import com.promotrack.api.service.OfferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OfferController.class)
@Import({OfferMapper.class, GlobalExceptionHandler.class})
class OfferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private OfferService offerService;

    @Test
    void findAllReturnsOffers() throws Exception {
        when(offerService.findAllActive()).thenReturn(List.of(offer(1L, "20% en lacteos")));

        mockMvc.perform(get("/api/offers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("20% en lacteos"))
                .andExpect(jsonPath("$[0].supermarketId").value(1));
    }

    @Test
    void findByIdReturnsOffer() throws Exception {
        when(offerService.findById(1L)).thenReturn(offer(1L, "2x1 en gaseosas"));

        mockMvc.perform(get("/api/offers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("2x1 en gaseosas"));
    }

    @Test
    void createReturnsCreatedOffer() throws Exception {
        Offer created = offer(1L, "Precio especial");
        when(offerService.create(any(Offer.class), eq(1L))).thenReturn(created);

        mockMvc.perform(post("/api/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/offers/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Precio especial"));
    }

    @Test
    void createReturnsBadRequestWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/api/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/offers"))
                .andExpect(jsonPath("$.detail").isNotEmpty())
                .andExpect(jsonPath("$.errors.title").value("title is required"));
    }

    @Test
    void updateReturnsUpdatedOffer() throws Exception {
        Offer updated = offer(1L, "3x2 en limpieza");
        when(offerService.update(eq(1L), any(Offer.class))).thenReturn(updated);

        mockMvc.perform(put("/api/offers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OfferUpdatePayload(
                                "3x2 en limpieza",
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("3x2 en limpieza"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/offers/1"))
                .andExpect(status().isNoContent());

        verify(offerService).delete(1L);
    }

    @Test
    void calendarReturnsOffersForDateRange() throws Exception {
        when(offerService.findCalendarOffers(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31)))
                .thenReturn(List.of(offer(1L, "Oferta calendario")));

        mockMvc.perform(get("/api/offers/calendar")
                        .param("from", "2026-05-01")
                        .param("to", "2026-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Oferta calendario"));
    }

    @Test
    void todayReturnsTodayOffers() throws Exception {
        when(offerService.findTodayActiveOffers()).thenReturn(List.of(offer(1L, "Oferta de hoy")));

        mockMvc.perform(get("/api/offers/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Oferta de hoy"));
    }

    @Test
    void upcomingReturnsUpcomingOffers() throws Exception {
        when(offerService.findUpcomingOffers()).thenReturn(List.of(offer(1L, "Oferta futura")));

        mockMvc.perform(get("/api/offers/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Oferta futura"));
    }

    @Test
    void expiringSoonReturnsExpiringSoonOffers() throws Exception {
        when(offerService.findExpiringSoonOffers(3)).thenReturn(List.of(offer(1L, "Oferta por vencer")));

        mockMvc.perform(get("/api/offers/expiring-soon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Oferta por vencer"));
    }

    @Test
    void expiringSoonReturnsOffersWithCustomDays() throws Exception {
        when(offerService.findExpiringSoonOffers(7)).thenReturn(List.of(offer(1L, "Oferta por vencer en 7 dias")));

        mockMvc.perform(get("/api/offers/expiring-soon")
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Oferta por vencer en 7 dias"));
    }

    @Test
    void expiringSoonReturnsBadRequestWhenDaysIsTooLow() throws Exception {
        mockMvc.perform(get("/api/offers/expiring-soon")
                        .param("days", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/offers/expiring-soon"));
    }

    @Test
    void expiringSoonReturnsBadRequestWhenDaysIsTooHigh() throws Exception {
        mockMvc.perform(get("/api/offers/expiring-soon")
                        .param("days", "31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/offers/expiring-soon"));
    }

    @Test
    void supermarketReturnsOffersBySupermarket() throws Exception {
        when(offerService.findOffersBySupermarket(1L)).thenReturn(List.of(offer(1L, "Oferta Carrefour")));

        mockMvc.perform(get("/api/offers/supermarket/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Oferta Carrefour"));
    }

    @Test
    void invalidCalendarRangeReturnsBadRequest() throws Exception {
        when(offerService.findCalendarOffers(LocalDate.of(2026, 5, 31), LocalDate.of(2026, 5, 1)))
                .thenThrow(new InvalidDateRangeException("endDate cannot be before startDate"));

        mockMvc.perform(get("/api/offers/calendar")
                        .param("from", "2026-05-31")
                        .param("to", "2026-05-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.path").value("/api/offers/calendar"));
    }

    private Offer offer(Long id, String title) {
        Supermarket supermarket = new Supermarket("Carrefour", "Descripcion", "https://example.com", "Argentina");
        supermarket.setId(1L);
        Offer offer = new Offer(
                title,
                "Descripcion",
                "Lacteos",
                DiscountType.PERCENTAGE,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 10),
                OfferSource.MANUAL,
                supermarket
        );
        offer.setId(id);
        offer.setDiscountValue(new BigDecimal("20.00"));
        return offer;
    }

    private OfferCreatePayload createPayload() {
        return new OfferCreatePayload(
                "Precio especial",
                "Descripcion",
                "Almacen",
                DiscountType.SPECIAL_PRICE,
                null,
                new BigDecimal("3500.00"),
                new BigDecimal("2899.99"),
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 10),
                OfferSource.MANUAL,
                1L
        );
    }

    private record OfferCreatePayload(
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
            Long supermarketId
    ) {
    }

    private record OfferUpdatePayload(
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
            Long supermarketId
    ) {
    }
}
