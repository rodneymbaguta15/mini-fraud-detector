package com.example.minifrauddetector.service;

import com.example.minifrauddetector.config.FraudRulesProperties;
import com.example.minifrauddetector.dto.FraudCheckRequest;
import com.example.minifrauddetector.dto.FraudCheckResponse;
import com.example.minifrauddetector.dto.RiskLevel;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FraudScoringService {

    private static final BigDecimal AMOUNT_MODERATE = new BigDecimal("300");
    private static final BigDecimal AMOUNT_HIGH = new BigDecimal("1000");
    private static final BigDecimal AMOUNT_VERY_HIGH = new BigDecimal("2000");

    private final FraudRulesProperties fraudRulesProperties;

    public FraudScoringService(FraudRulesProperties fraudRulesProperties) {
        this.fraudRulesProperties = fraudRulesProperties;
    }

    public FraudCheckResponse evaluate(FraudCheckRequest request) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        BigDecimal amount = request.getAmount();
        if (amount.compareTo(AMOUNT_VERY_HIGH) >= 0) {
            score += 35;
            reasons.add("High amount (>= 2000)");
        } else if (amount.compareTo(AMOUNT_HIGH) >= 0) {
            score += 25;
            reasons.add("High amount (>= 1000)");
        } else if (amount.compareTo(AMOUNT_MODERATE) >= 0) {
            score += 10;
            reasons.add("Moderate amount (>= 300)");
        }

        if (!request.getDeviceTrusted()) {
            score += 20;
            reasons.add("Untrusted device");
        }

        String country = request.getCountry();
        if (fraudRulesProperties.getHighRiskCountries().contains(country)) {
            score += 25;
            reasons.add("High-risk country: " + country);
        }

        int hourUtc = ZonedDateTime.ofInstant(request.getTimestamp(), ZoneOffset.UTC).getHour();
        FraudRulesProperties.NightWindow nightWindow = fraudRulesProperties.getNightWindow();
        int startHourUtc = nightWindow.getStartHourUtc();
        int endHourUtc = nightWindow.getEndHourUtc();

        if (isWithinWindow(hourUtc, startHourUtc, endHourUtc)) {
            score += 10;
            reasons.add(String.format(
                "Transaction time is unusual (%02d:00 UTC in %02d:00–%02d:00 UTC)",
                hourUtc,
                startHourUtc,
                endHourUtc
            ));
        }

        int cappedScore = Math.min(score, 100);

        FraudCheckResponse response = new FraudCheckResponse();
        response.setTransactionId(request.getTransactionId());
        response.setRiskScore(cappedScore);
        response.setRiskLevel(resolveRiskLevel(cappedScore));
        response.setReasons(reasons);
        response.setEvaluatedAt(Instant.now());
        return response;
    }

    private boolean isWithinWindow(int hour, int start, int end) {
        if (start <= end) {
            return hour >= start && hour <= end;
        }
        return hour >= start || hour <= end;
    }

    private RiskLevel resolveRiskLevel(int score) {
        if (score >= 70) {
            return RiskLevel.HIGH;
        }
        if (score >= 30) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }
}
