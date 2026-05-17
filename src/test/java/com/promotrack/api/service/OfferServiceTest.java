package com.promotrack.api.service;

import com.promotrack.api.domain.enums.DiscountType;
import com.promotrack.api.domain.enums.OfferSource;
import com.promotrack.api.domain.model.Offer;
import com.promotrack.api.domain.model.Supermarket;
import com.promotrack.api.exception.InvalidDateRangeException;
import com.promotrack.api.exception.ResourceNotFoundException;
import com.promotrack.api.repository.OfferRepository;
import com.promotrack.api.repository.SupermarketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

    private static final LocalDate FIXED_TODAY = LocalDate.of(2026, 5, 3);

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private SupermarketRepository supermarketRepository;

    private OfferService offerService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-05-03T12:00:00Z"), ZoneId.of("UTC"));
        offerService = new OfferService(offerRepository, supermarketRepository, clock);
    }

    @Test
    void findAllActiveReturnsActiveOffers() {
        Offer offer = validOffer();
        when(offerRepository.findByActiveTrue()).thenReturn(List.of(offer));

        List<Offer> result = offerService.findAllActive();

        assertThat(result).containsExactly(offer);
    }

    @Test
    void findByIdReturnsOfferWhenItExists() {
        Offer offer = validOffer();
        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));

        Offer result = offerService.findById(1L);

        assertThat(result).isSameAs(offer);
    }

    @Test
    void findByIdThrowsResourceNotFoundExceptionWhenMissing() {
        when(offerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Offer not found");
    }

    @Test
    void createAssociatesExistingSupermarketAndSavesOffer() {
        Offer offer = validOffer();
        Supermarket supermarket = supermarket();
        when(supermarketRepository.findById(1L)).thenReturn(Optional.of(supermarket));
        when(offerRepository.save(offer)).thenReturn(offer);

        Offer result = offerService.create(offer, 1L);

        assertThat(result.getSupermarket()).isSameAs(supermarket);
        assertThat(result.isActive()).isTrue();
        verify(offerRepository).save(offer);
    }

    @Test
    void createThrowsResourceNotFoundExceptionWhenSupermarketDoesNotExist() {
        when(supermarketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.create(validOffer(), 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supermarket not found");
    }

    @Test
    void createThrowsValidationExceptionWhenEndDateIsBeforeStartDate() {
        Offer offer = validOffer();
        offer.setStartDate(LocalDate.of(2026, 5, 10));
        offer.setEndDate(LocalDate.of(2026, 5, 5));

        assertThatThrownBy(() -> offerService.create(offer, 1L))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessageContaining("endDate cannot be before startDate");
    }

    @Test
    void updateCopiesEditableFieldsAndSaves() {
        Offer existing = validOffer();
        Offer updated = validOffer();
        updated.setTitle("3x2 en limpieza");
        updated.setDescription("Promocion semanal");
        updated.setCategory("Limpieza");
        updated.setDiscountType(DiscountType.THREE_FOR_TWO);
        updated.setDiscountValue(new BigDecimal("30.00"));
        updated.setOriginalPrice(new BigDecimal("4500.00"));
        updated.setFinalPrice(new BigDecimal("3000.00"));
        updated.setStartDate(LocalDate.of(2026, 5, 4));
        updated.setEndDate(LocalDate.of(2026, 5, 8));
        updated.setSource(OfferSource.IMPORT);
        when(offerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Offer result = offerService.update(1L, updated);

        assertThat(result.getTitle()).isEqualTo("3x2 en limpieza");
        assertThat(result.getDescription()).isEqualTo("Promocion semanal");
        assertThat(result.getCategory()).isEqualTo("Limpieza");
        assertThat(result.getDiscountType()).isEqualTo(DiscountType.THREE_FOR_TWO);
        assertThat(result.getDiscountValue()).isEqualByComparingTo("30.00");
        assertThat(result.getOriginalPrice()).isEqualByComparingTo("4500.00");
        assertThat(result.getFinalPrice()).isEqualByComparingTo("3000.00");
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 5, 4));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2026, 5, 8));
        assertThat(result.getSource()).isEqualTo(OfferSource.IMPORT);
        verify(offerRepository).save(existing);
    }

    @Test
    void updateThrowsValidationExceptionWhenEndDateIsBeforeStartDate() {
        Offer existing = validOffer();
        Offer updated = validOffer();
        updated.setStartDate(LocalDate.of(2026, 5, 10));
        updated.setEndDate(LocalDate.of(2026, 5, 5));
        when(offerRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> offerService.update(1L, updated))
                .isInstanceOf(InvalidDateRangeException.class);
    }

    @Test
    void deleteMarksOfferAsInactive() {
        Offer offer = validOffer();
        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));

        offerService.delete(1L);

        assertThat(offer.isActive()).isFalse();
        verify(offerRepository).save(offer);
    }

    @Test
    void findTodayActiveOffersUsesTodayAsInclusiveRange() {
        Offer offer = validOffer();
        when(offerRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(FIXED_TODAY, FIXED_TODAY))
                .thenReturn(List.of(offer));

        List<Offer> result = offerService.findTodayActiveOffers();

        assertThat(result).containsExactly(offer);
    }

    @Test
    void findUpcomingOffersUsesStartDateAfterToday() {
        Offer offer = validOffer();
        when(offerRepository.findByActiveTrueAndStartDateAfter(FIXED_TODAY)).thenReturn(List.of(offer));

        List<Offer> result = offerService.findUpcomingOffers();

        assertThat(result).containsExactly(offer);
    }

    @Test
    void findExpiringSoonOffersUsesNextThreeDaysFromToday() {
        Offer offer = validOffer();
        when(offerRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateBetween(
                FIXED_TODAY,
                FIXED_TODAY,
                FIXED_TODAY.plusDays(3)
        )).thenReturn(List.of(offer));

        List<Offer> result = offerService.findExpiringSoonOffers();

        assertThat(result).containsExactly(offer);
    }

    @Test
    void findExpiringSoonOffersUsesCustomDaysFromToday() {
        Offer offer = validOffer();
        when(offerRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateBetween(
                FIXED_TODAY,
                FIXED_TODAY,
                FIXED_TODAY.plusDays(7)
        )).thenReturn(List.of(offer));

        List<Offer> result = offerService.findExpiringSoonOffers(7);

        assertThat(result).containsExactly(offer);
    }

    @Test
    void findCalendarOffersReturnsOffersOverlappingTheRequestedRange() {
        Offer offer = validOffer();
        LocalDate from = LocalDate.of(2026, 5, 1);
        LocalDate to = LocalDate.of(2026, 5, 31);
        when(offerRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAsc(to, from))
                .thenReturn(List.of(offer));

        List<Offer> result = offerService.findCalendarOffers(from, to);

        assertThat(result).containsExactly(offer);
    }

    @Test
    void findCalendarOffersThrowsValidationExceptionWhenRangeIsInvalid() {
        assertThatThrownBy(() -> offerService.findCalendarOffers(
                LocalDate.of(2026, 5, 31),
                LocalDate.of(2026, 5, 1)
        )).isInstanceOf(InvalidDateRangeException.class);
    }

    @Test
    void findOffersBySupermarketValidatesSupermarketAndReturnsActiveOffers() {
        Offer offer = validOffer();
        Supermarket supermarket = supermarket();
        when(supermarketRepository.findById(1L)).thenReturn(Optional.of(supermarket));
        when(offerRepository.findByActiveTrueAndSupermarketId(1L)).thenReturn(List.of(offer));

        List<Offer> result = offerService.findOffersBySupermarket(1L);

        assertThat(result).containsExactly(offer);
    }

    private Offer validOffer() {
        return new Offer(
                "20% en lacteos",
                "Descuento aplicado en caja",
                "Lacteos",
                DiscountType.PERCENTAGE,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 10),
                OfferSource.MANUAL,
                supermarket()
        );
    }

    private Supermarket supermarket() {
        return new Supermarket(
                "Carrefour Argentina",
                "Cadena de supermercados con presencia nacional",
                "https://www.carrefour.com.ar",
                "Argentina"
        );
    }
}
