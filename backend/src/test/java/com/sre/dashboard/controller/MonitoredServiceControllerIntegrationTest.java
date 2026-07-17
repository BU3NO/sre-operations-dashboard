package com.sre.dashboard.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MonitoredServiceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldListSeededServices() throws Exception {
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[0].name").value("Customer API"))
                .andExpect(jsonPath("$[0].status").value("UP"));
    }

    @Test
    void shouldCreateUpdateStatusAndDeleteService() throws Exception {
        String body = """
                {
                  "name": "Search API",
                  "description": "Servico de busca",
                  "environment": "DEVELOPMENT",
                  "url": "http://localhost:8081/health",
                  "status": "UP"
                }
                """;

        String location = mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Search API"))
                .andReturn().getResponse().getHeader("Location");

        String id = location.substring(location.lastIndexOf('/') + 1);

        mockMvc.perform(patch("/api/services/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DEGRADED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEGRADED"));

        mockMvc.perform(delete("/api/services/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/services/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"url\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void shouldExposeDashboardSummary() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.up").isNumber())
                .andExpect(jsonPath("$.degraded").isNumber())
                .andExpect(jsonPath("$.down").isNumber());
    }

    @Test
    void shouldExposeSystemInfo() throws Exception {
        mockMvc.perform(get("/api/system/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("SRE Operations Dashboard"))
                .andExpect(jsonPath("$.environment").value("TEST"))
                .andExpect(jsonPath("$.javaVersion").exists());
    }
}
