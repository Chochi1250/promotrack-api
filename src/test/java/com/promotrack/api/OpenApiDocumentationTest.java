package com.promotrack.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApiDocsExposeApiMetadataAndOperations() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("PromoTrack API"))
                .andExpect(jsonPath("$.info.version").value("0.0.1-SNAPSHOT"))
                .andExpect(jsonPath("$.paths", hasKey("/api/supermarkets")))
                .andExpect(jsonPath("$.paths", hasKey("/api/supermarkets/{id}")))
                .andExpect(jsonPath("$.paths", hasKey("/api/offers")))
                .andExpect(jsonPath("$.paths", hasKey("/api/offers/{id}")))
                .andExpect(jsonPath("$.paths", hasKey("/api/offers/expiring-soon")))
                .andExpect(jsonPath("$.paths", hasKey("/api/offers/calendar")))
                .andExpect(jsonPath("$.paths['/api/offers/expiring-soon'].get.parameters[?(@.name == 'days')]").exists())
                .andExpect(jsonPath("$.tags[?(@.name == 'Supermarkets')]").exists())
                .andExpect(jsonPath("$.tags[?(@.name == 'Offers')]").exists());
    }
}
