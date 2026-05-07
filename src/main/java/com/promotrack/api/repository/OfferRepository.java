package com.promotrack.api.repository;

import com.promotrack.api.domain.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    List<Offer> findByActiveTrue();

    List<Offer> findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate startDate, LocalDate endDate);

    List<Offer> findByActiveTrueAndStartDateAfter(LocalDate date);

    List<Offer> findByActiveTrueAndStartDateLessThanEqualAndEndDateBetween(
            LocalDate startDate,
            LocalDate endDateFrom,
            LocalDate endDateTo
    );

    List<Offer> findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAsc(
            LocalDate to,
            LocalDate from
    );

    List<Offer> findByActiveTrueAndSupermarketId(Long supermarketId);
}
