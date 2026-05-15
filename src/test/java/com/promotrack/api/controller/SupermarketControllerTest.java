package com.promotrack.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promotrack.api.domain.model.Supermarket;
import com.promotrack.api.exception.GlobalExceptionHandler;
import com.promotrack.api.exception.ResourceNotFoundException;
import com.promotrack.api.mapper.SupermarketMapper;
import com.promotrack.api.service.SupermarketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupermarketController.class)
@Import({SupermarketMapper.class, GlobalExceptionHandler.class})
class SupermarketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private SupermarketService supermarketService;

    @Test
    void findAllReturnsActiveSupermarkets() throws Exception {
        Supermarket supermarket = supermarket(1L, "Coto");
        when(supermarketService.findAllActive()).thenReturn(List.of(supermarket));

        mockMvc.perform(get("/api/supermarkets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Coto"));
    }

    @Test
    void findByIdReturnsSupermarket() throws Exception {
        when(supermarketService.findById(1L)).thenReturn(supermarket(1L, "Disco"));

        mockMvc.perform(get("/api/supermarkets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Disco"));
    }

    @Test
    void createReturnsCreatedSupermarket() throws Exception {
        Supermarket created = supermarket(1L, "Carrefour");
        when(supermarketService.create(any(Supermarket.class))).thenReturn(created);

        mockMvc.perform(post("/api/supermarkets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SupermarketPayload(
                                "Carrefour",
                                "Cadena nacional",
                                "https://www.carrefour.com.ar",
                                "Argentina"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/supermarkets/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Carrefour"));
    }

    @Test
    void createReturnsBadRequestWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/api/supermarkets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/supermarkets"))
                .andExpect(jsonPath("$.detail").isNotEmpty())
                .andExpect(jsonPath("$.errors.name").value("name is required"));
    }

    @Test
    void updateReturnsUpdatedSupermarket() throws Exception {
        Supermarket updated = supermarket(1L, "Coto Digital");
        when(supermarketService.update(any(Long.class), any(Supermarket.class))).thenReturn(updated);

        mockMvc.perform(put("/api/supermarkets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SupermarketPayload(
                                "Coto Digital",
                                null,
                                null,
                                null
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Coto Digital"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/supermarkets/1"))
                .andExpect(status().isNoContent());

        verify(supermarketService).delete(1L);
    }

    @Test
    void missingSupermarketReturnsNotFound() throws Exception {
        when(supermarketService.findById(99L)).thenThrow(new ResourceNotFoundException("Supermarket not found with id: 99"));

        mockMvc.perform(get("/api/supermarkets/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.path").value("/api/supermarkets/99"));
    }

    private Supermarket supermarket(Long id, String name) {
        Supermarket supermarket = new Supermarket(name, "Descripcion", "https://example.com", "Argentina");
        supermarket.setId(id);
        return supermarket;
    }

    private record SupermarketPayload(String name, String description, String website, String country) {
    }
}
