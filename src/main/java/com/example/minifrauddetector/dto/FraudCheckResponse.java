package com.example.minifrauddetector.dto;

import java.time.Instant;
import java.util.List;
import lombok.Data;

@Data
public class FraudCheckResponse {

    private String transactionId;
    private int riskScore;
    private RiskLevel riskLevel;
    private List<String> reasons;
    private Instant evaluatedAt;
}
