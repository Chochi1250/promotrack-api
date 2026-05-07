package com.promotrack.api.repository;

import com.promotrack.api.domain.model.Supermarket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupermarketRepository extends JpaRepository<Supermarket, Long> {

    List<Supermarket> findByActiveTrue();
}
