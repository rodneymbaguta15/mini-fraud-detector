package com.example.minifrauddetector.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FraudCheckRequestJacksonTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldDeserializeFraudCheckRequest() throws Exception {
        String payload = """
            {
              \"transactionId\": \"tx-123\",
              \"userId\": \"user-42\",
              \"amount\": 149.99,
              \"currency\": \"USD\",
              \"merchantCategory\": \"electronics\",
              \"country\": \"US\",
              \"timestamp\": \"2026-01-01T12:00:00Z\",
              \"deviceTrusted\": false,
              \"paymentMethod\": \"CARD\",
              \"ipAddress\": \"203.0.113.7\"
            }
            """;

        FraudCheckRequest request = objectMapper.readValue(payload, FraudCheckRequest.class);

        assertEquals("tx-123", request.getTransactionId());
        assertEquals("user-42", request.getUserId());
        assertEquals(new BigDecimal("149.99"), request.getAmount());
        assertEquals("USD", request.getCurrency());
        assertEquals("US", request.getCountry());
        assertFalse(request.getDeviceTrusted());
    }
}
