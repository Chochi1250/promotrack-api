package com.promotrack.api.service;

import com.promotrack.api.domain.model.Supermarket;
import com.promotrack.api.exception.ResourceNotFoundException;
import com.promotrack.api.repository.SupermarketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupermarketServiceTest {

    @Mock
    private SupermarketRepository supermarketRepository;

    @InjectMocks
    private SupermarketService supermarketService;

    @Test
    void findAllActiveReturnsActiveSupermarkets() {
        Supermarket supermarket = new Supermarket("Coto", "Supermercado argentino", "https://www.coto.com.ar", "Argentina");
        when(supermarketRepository.findByActiveTrue()).thenReturn(List.of(supermarket));

        List<Supermarket> result = supermarketService.findAllActive();

        assertThat(result).containsExactly(supermarket);
    }

    @Test
    void findByIdReturnsSupermarketWhenItExists() {
        Supermarket supermarket = new Supermarket("Disco", "Cadena nacional", "https://www.disco.com.ar", "Argentina");
        when(supermarketRepository.findById(1L)).thenReturn(Optional.of(supermarket));

        Supermarket result = supermarketService.findById(1L);

        assertThat(result).isSameAs(supermarket);
    }

    @Test
    void findByIdThrowsResourceNotFoundExceptionWhenMissing() {
        when(supermarketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supermarketService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supermarket not found");
    }

    @Test
    void createActivatesAndSavesSupermarket() {
        Supermarket supermarket = new Supermarket("Carrefour", "Cadena nacional", "https://www.carrefour.com.ar", "Argentina");
        supermarket.setActive(false);
        when(supermarketRepository.save(supermarket)).thenReturn(supermarket);

        Supermarket result = supermarketService.create(supermarket);

        assertThat(result.isActive()).isTrue();
        verify(supermarketRepository).save(supermarket);
    }

    @Test
    void updateCopiesEditableFieldsAndSaves() {
        Supermarket existing = new Supermarket("Viejo", "Descripcion vieja", "https://old.example", "Argentina");
        Supermarket updated = new Supermarket("Nuevo", "Descripcion nueva", "https://new.example", "Argentina");
        when(supermarketRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(supermarketRepository.save(any(Supermarket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Supermarket result = supermarketService.update(1L, updated);

        assertThat(result.getName()).isEqualTo("Nuevo");
        assertThat(result.getDescription()).isEqualTo("Descripcion nueva");
        assertThat(result.getWebsite()).isEqualTo("https://new.example");
        assertThat(result.getCountry()).isEqualTo("Argentina");
        verify(supermarketRepository).save(existing);
    }

    @Test
    void deleteMarksSupermarketAsInactive() {
        Supermarket supermarket = new Supermarket("Coto", "Supermercado argentino", "https://www.coto.com.ar", "Argentina");
        when(supermarketRepository.findById(1L)).thenReturn(Optional.of(supermarket));

        supermarketService.delete(1L);

        assertThat(supermarket.isActive()).isFalse();
        verify(supermarketRepository).save(supermarket);
    }
}
