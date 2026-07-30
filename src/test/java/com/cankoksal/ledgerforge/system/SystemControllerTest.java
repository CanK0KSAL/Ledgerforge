package com.cankoksal.ledgerforge.system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SystemController.class)
class SystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SystemService systemService;

    @Test
    void echo_shouldReturnProcessedMessage_whenRequestIsValid() throws Exception {
        given(systemService.createEcho(any(EchoRequest.class)))
                .willReturn(new EchoResponse(
                        "  LedgerForge  ",
                        "LedgerForge",
                        11
                ));

        mockMvc.perform(post("/api/system/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "  LedgerForge  "
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.originalMessage")
                        .value("  LedgerForge  "))
                .andExpect(jsonPath("$.normalizedMessage")
                        .value("LedgerForge"))
                .andExpect(jsonPath("$.length")
                        .value(11));
    }

    @Test
    void echo_shouldReturnValidationError_whenMessageIsBlank()
            throws Exception {

        mockMvc.perform(post("/api/system/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.path")
                        .value("/api/system/echo"))
                .andExpect(jsonPath("$.fieldErrors.message")
                        .value("Message must not be blank"));

        verifyNoInteractions(systemService);
    }

    @Test
    void echo_shouldReturnMalformedJsonError_whenJsonIsInvalid()
            throws Exception {

        mockMvc.perform(post("/api/system/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "LedgerForge"
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("MALFORMED_JSON"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Request body is missing or contains invalid JSON"
                        ))
                .andExpect(jsonPath("$.path")
                        .value("/api/system/echo"));

        verifyNoInteractions(systemService);
    }
}