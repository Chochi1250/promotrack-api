package com.promotrack.api.service;

import com.promotrack.api.domain.model.Offer;
import com.promotrack.api.domain.model.Supermarket;
import com.promotrack.api.exception.InvalidDateRangeException;
import com.promotrack.api.exception.ResourceNotFoundException;
import com.promotrack.api.repository.OfferRepository;
import com.promotrack.api.repository.SupermarketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class OfferService {

    private static final int EXPIRING_SOON_DAYS = 3;

    private final OfferRepository offerRepository;
    private final SupermarketRepository supermarketRepository;
    private final Clock clock;

    @Autowired
    public OfferService(OfferRepository offerRepository, SupermarketRepository supermarketRepository) {
        this(offerRepository, supermarketRepository, Clock.systemDefaultZone());
    }

    OfferService(OfferRepository offerRepository, SupermarketRepository supermarketRepository, Clock clock) {
        this.offerRepository = offerRepository;
        this.supermarketRepository = supermarketRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Offer> findAllActive() {
        return offerRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Offer findById(Long id) {
        return getExistingOffer(id);
    }

    public Offer create(Offer offer, Long supermarketId) {
        validateDateRange(offer);
        Supermarket supermarket = getExistingSupermarket(supermarketId);
        offer.setSupermarket(supermarket);
        offer.setActive(true);
        return offerRepository.save(offer);
    }

    public Offer update(Long id, Offer updatedOffer) {
        Offer offer = getExistingOffer(id);
        if (updatedOffer.getTitle() != null) {
            offer.setTitle(updatedOffer.getTitle());
        }
        if (updatedOffer.getDescription() != null) {
            offer.setDescription(updatedOffer.getDescription());
        }
        if (updatedOffer.getCategory() != null) {
            offer.setCategory(updatedOffer.getCategory());
        }
        if (updatedOffer.getDiscountType() != null) {
            offer.setDiscountType(updatedOffer.getDiscountType());
        }
        if (updatedOffer.getDiscountValue() != null) {
            offer.setDiscountValue(updatedOffer.getDiscountValue());
        }
        if (updatedOffer.getOriginalPrice() != null) {
            offer.setOriginalPrice(updatedOffer.getOriginalPrice());
        }
        if (updatedOffer.getFinalPrice() != null) {
            offer.setFinalPrice(updatedOffer.getFinalPrice());
        }
        if (updatedOffer.getStartDate() != null) {
            offer.setStartDate(updatedOffer.getStartDate());
        }
        if (updatedOffer.getEndDate() != null) {
            offer.setEndDate(updatedOffer.getEndDate());
        }
        if (updatedOffer.getSource() != null) {
            offer.setSource(updatedOffer.getSource());
        }

        if (updatedOffer.getSupermarket() != null && updatedOffer.getSupermarket().getId() != null) {
            Supermarket supermarket = getExistingSupermarket(updatedOffer.getSupermarket().getId());
            offer.setSupermarket(supermarket);
        }

        validateDateRange(offer);
        return offerRepository.save(offer);
    }

    public void delete(Long id) {
        Offer offer = getExistingOffer(id);
        offer.setActive(false);
        offerRepository.save(offer);
    }

    @Transactional(readOnly = true)
    public List<Offer> findTodayActiveOffers() {
        LocalDate today = today();
        return offerRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);
    }

    @Transactional(readOnly = true)
    public List<Offer> findUpcomingOffers() {
        return offerRepository.findByActiveTrueAndStartDateAfter(today());
    }

    @Transactional(readOnly = true)
    public List<Offer> findExpiringSoonOffers() {
        return findExpiringSoonOffers(EXPIRING_SOON_DAYS);
    }

    @Transactional(readOnly = true)
    public List<Offer> findExpiringSoonOffers(int days) {
        LocalDate today = today();
        return offerRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateBetween(
                today,
                today,
                today.plusDays(days)
        );
    }

    @Transactional(readOnly = true)
    public List<Offer> findCalendarOffers(LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        return offerRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAsc(
                to,
                from
        );
    }

    @Transactional(readOnly = true)
    public List<Offer> findOffersBySupermarket(Long supermarketId) {
        getExistingSupermarket(supermarketId);
        return offerRepository.findByActiveTrueAndSupermarketId(supermarketId);
    }

    private Offer getExistingOffer(Long id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + id));
    }

    private Supermarket getExistingSupermarket(Long id) {
        return supermarketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supermarket not found with id: " + id));
    }

    private void validateDateRange(Offer offer) {
        validateDateRange(offer.getStartDate(), offer.getEndDate());
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidDateRangeException("endDate cannot be before startDate");
        }
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
