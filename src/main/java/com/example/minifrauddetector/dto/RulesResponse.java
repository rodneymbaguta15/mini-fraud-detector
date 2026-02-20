package com.example.minifrauddetector.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
@Schema(description = "Fraud scoring rules and thresholds used by the API")
public class RulesResponse {

    @Schema(description = "Transaction amount thresholds used by amount-based rules")
    private Map<String, Integer> amountThresholds;

    @Schema(description = "Points added for each rule when triggered")
    private Map<String, Integer> points;

    @Schema(description = "Configured high-risk ISO country codes")
    private List<String> highRiskCountries;

    @Schema(description = "UTC night window used for unusual-time scoring")
    private NightWindowDto nightWindow;

    @Schema(description = "Risk score ranges mapped to risk bands")
    private Map<String, String> riskBands;

    @Data
    public static class NightWindowDto {

        private int startHourUtc;
        private int endHourUtc;
    }
}
