package com.example.minifrauddetector.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.minifrauddetector.dto.FraudCheckResponse;
import com.example.minifrauddetector.dto.RiskLevel;
import com.example.minifrauddetector.service.FraudScoringService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FraudController.class)
class FraudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FraudScoringService fraudScoringService;

    @Test
    void shouldReturnFraudCheckResponseForValidRequest() throws Exception {
        FraudCheckResponse response = new FraudCheckResponse();
        response.setTransactionId("tx-123");
        response.setRiskScore(45);
        response.setRiskLevel(RiskLevel.MEDIUM);
        response.setReasons(List.of("Untrusted device"));
        response.setEvaluatedAt(Instant.parse("2026-01-01T12:00:00Z"));

        when(fraudScoringService.evaluate(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/fraud/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "transactionId": "tx-123",
                      "userId": "user-1",
                      "amount": 1200.00,
                      "currency": "USD",
                      "merchantCategory": "electronics",
                      "country": "US",
                      "timestamp": "2026-01-01T12:00:00Z",
                      "deviceTrusted": false,
                      "paymentMethod": "card",
                      "ipAddress": "203.0.113.7"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transactionId").value("tx-123"))
            .andExpect(jsonPath("$.riskScore").value(45))
            .andExpect(jsonPath("$.riskLevel").value("MEDIUM"))
            .andExpect(jsonPath("$.reasons").isArray())
            .andExpect(jsonPath("$.evaluatedAt").exists());
    }

    @Test
    void shouldReturnBadRequestWithFieldErrorsForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/v1/fraud/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "transactionId": "tx-123",
                      "userId": "user-1",
                      "amount": -1,
                      "currency": "USD",
                      "country": "US",
                      "deviceTrusted": false
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.path").value("/api/v1/fraud/check"))
            .andExpect(jsonPath("$.fieldErrors", hasSize(2)))
            .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("amount")))
            .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("timestamp")));
    }

    @Test
    void shouldReturnBadRequestForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/fraud/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "transactionId": "tx-123",
                      "userId": "user-1",
                      "amount": 50.0,
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Malformed JSON request"));
    }
}
