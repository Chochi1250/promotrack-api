package com.promotrack.api.service;

import com.promotrack.api.domain.model.Supermarket;
import com.promotrack.api.exception.ResourceNotFoundException;
import com.promotrack.api.repository.SupermarketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SupermarketService {

    private final SupermarketRepository supermarketRepository;

    public SupermarketService(SupermarketRepository supermarketRepository) {
        this.supermarketRepository = supermarketRepository;
    }

    @Transactional(readOnly = true)
    public List<Supermarket> findAllActive() {
        return supermarketRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Supermarket findById(Long id) {
        return getExistingSupermarket(id);
    }

    public Supermarket create(Supermarket supermarket) {
        supermarket.setActive(true);
        return supermarketRepository.save(supermarket);
    }

    public Supermarket update(Long id, Supermarket updatedSupermarket) {
        Supermarket supermarket = getExistingSupermarket(id);
        if (updatedSupermarket.getName() != null) {
            supermarket.setName(updatedSupermarket.getName());
        }
        if (updatedSupermarket.getDescription() != null) {
            supermarket.setDescription(updatedSupermarket.getDescription());
        }
        if (updatedSupermarket.getWebsite() != null) {
            supermarket.setWebsite(updatedSupermarket.getWebsite());
        }
        if (updatedSupermarket.getCountry() != null) {
            supermarket.setCountry(updatedSupermarket.getCountry());
        }
        return supermarketRepository.save(supermarket);
    }

    public void delete(Long id) {
        Supermarket supermarket = getExistingSupermarket(id);
        supermarket.setActive(false);
        supermarketRepository.save(supermarket);
    }

    private Supermarket getExistingSupermarket(Long id) {
        return supermarketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supermarket not found with id: " + id));
    }
}
