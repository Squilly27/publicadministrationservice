package com.ifm.publicadministrationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifm.publicadministrationservice.dto.CreateRichiestaDTO;
import com.ifm.publicadministrationservice.service.RichiestaAccessoAttiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RichiestaAccessoAttiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RichiestaAccessoAttiService richiestaService;

    @Test
    @WithMockUser(roles = "OPERATORE")
    void testCreateRichiestaWithOperatoreRole() throws Exception {
        CreateRichiestaDTO dto = CreateRichiestaDTO.builder()
                .nomeRichiedente("Giovanni")
                .cognomeRichiedente("Rossi")
                .emailRichiedente("giovanni@example.com")
                .oggetto("Test")
                .descrizione("Test descrizione")
                .build();

        mockMvc.perform(post("/api/richieste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "RESPONSABILE")
    void testCreateRichiestaWithResponsabileRoleUnauthorized() throws Exception {
        CreateRichiestaDTO dto = CreateRichiestaDTO.builder()
                .nomeRichiedente("Giovanni")
                .cognomeRichiedente("Rossi")
                .emailRichiedente("giovanni@example.com")
                .oggetto("Test")
                .descrizione("Test descrizione")
                .build();

        mockMvc.perform(post("/api/richieste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetRichiesteWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/richieste"))
                .andExpect(status().isUnauthorized());
    }
}

