package com.example.minifrauddetector.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.minifrauddetector.config.FraudRulesProperties;
import com.example.minifrauddetector.dto.FraudCheckRequest;
import com.example.minifrauddetector.dto.FraudCheckResponse;
import com.example.minifrauddetector.dto.RiskLevel;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FraudScoringServiceTest {

    private FraudScoringService service;

    @BeforeEach
    void setUp() {
        FraudRulesProperties properties = new FraudRulesProperties();
        properties.setHighRiskCountries(List.of("MM", "GH", "KE", "ZA", "BR", "CY"));

        FraudRulesProperties.NightWindow nightWindow = new FraudRulesProperties.NightWindow();
        nightWindow.setStartHourUtc(0);
        nightWindow.setEndHourUtc(5);
        properties.setNightWindow(nightWindow);

        service = new FraudScoringService(properties);
    }

    @Test
    void shouldHandleAmountBoundaries() {
        assertEquals(0, evaluateAmount("299").getRiskScore());
        assertEquals(10, evaluateAmount("300").getRiskScore());
        assertEquals(10, evaluateAmount("999").getRiskScore());
        assertEquals(25, evaluateAmount("1000").getRiskScore());
        assertEquals(25, evaluateAmount("1999").getRiskScore());
        assertEquals(35, evaluateAmount("2000").getRiskScore());
    }

    @Test
    void shouldApplyDeviceTrustRule() {
        FraudCheckResponse trusted = service.evaluate(baseRequestBuilder()
            .deviceTrusted(true)
            .build());

        FraudCheckResponse untrusted = service.evaluate(baseRequestBuilder()
            .deviceTrusted(false)
            .build());

        assertEquals(0, trusted.getRiskScore());
        assertEquals(20, untrusted.getRiskScore());
        assertTrue(untrusted.getReasons().contains("Untrusted device"));
    }

    @Test
    void shouldApplyHighRiskCountryRule() {
        FraudCheckResponse highRisk = service.evaluate(baseRequestBuilder()
            .country("GH")
            .build());

        FraudCheckResponse normal = service.evaluate(baseRequestBuilder()
            .country("US")
            .build());

        assertEquals(25, highRisk.getRiskScore());
        assertTrue(highRisk.getReasons().contains("High-risk country: GH"));
        assertEquals(0, normal.getRiskScore());
    }

    @Test
    void shouldApplyNightWindowRuleInsideAndOutside() {
        FraudCheckResponse inside = service.evaluate(baseRequestBuilder()
            .timestamp(Instant.parse("2026-01-01T02:00:00Z"))
            .build());

        FraudCheckResponse outside = service.evaluate(baseRequestBuilder()
            .timestamp(Instant.parse("2026-01-01T12:00:00Z"))
            .build());

        assertEquals(10, inside.getRiskScore());
        assertTrue(inside.getReasons().stream()
            .anyMatch(reason -> reason.contains("Transaction time is unusual (02:00 UTC in 00:00–05:00 UTC)")));
        assertEquals(0, outside.getRiskScore());
    }

    @Test
    void shouldCapScoreAtOneHundred() {
        FraudCheckResponse response = service.evaluate(baseRequestBuilder()
            .amount(new BigDecimal("5000"))
            .deviceTrusted(false)
            .country("GH")
            .timestamp(Instant.parse("2026-01-01T02:00:00Z"))
            .build());

        assertTrue(response.getRiskScore() <= 100);
        assertEquals(90, response.getRiskScore());
        assertEquals(RiskLevel.HIGH, response.getRiskLevel());
    }

    private FraudCheckResponse evaluateAmount(String amount) {
        return service.evaluate(baseRequestBuilder()
            .amount(new BigDecimal(amount))
            .build());
    }

    private RequestBuilder baseRequestBuilder() {
        return new RequestBuilder();
    }

    private static class RequestBuilder {

        private final FraudCheckRequest request;

        private RequestBuilder() {
            request = new FraudCheckRequest();
            request.setTransactionId("tx-1");
            request.setUserId("user-1");
            request.setAmount(new BigDecimal("1"));
            request.setCurrency("USD");
            request.setCountry("US");
            request.setTimestamp(Instant.parse("2026-01-01T12:00:00Z"));
            request.setDeviceTrusted(true);
        }

        private RequestBuilder amount(BigDecimal amount) {
            request.setAmount(amount);
            return this;
        }

        private RequestBuilder country(String country) {
            request.setCountry(country);
            return this;
        }

        private RequestBuilder timestamp(Instant timestamp) {
            request.setTimestamp(timestamp);
            return this;
        }

        private RequestBuilder deviceTrusted(boolean deviceTrusted) {
            request.setDeviceTrusted(deviceTrusted);
            return this;
        }

        private FraudCheckRequest build() {
            return request;
        }
    }
}
