package com.example.minifrauddetector.controller;

import com.example.minifrauddetector.config.FraudRulesProperties;
import com.example.minifrauddetector.dto.FraudCheckRequest;
import com.example.minifrauddetector.dto.FraudCheckResponse;
import com.example.minifrauddetector.dto.RulesResponse;
import com.example.minifrauddetector.exception.ApiErrorResponse;
import com.example.minifrauddetector.service.FraudScoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fraud")
public class FraudController {

    private final FraudScoringService fraudScoringService;
    private final FraudRulesProperties fraudRulesProperties;

    public FraudController(FraudScoringService fraudScoringService, FraudRulesProperties fraudRulesProperties) {
        this.fraudScoringService = fraudScoringService;
        this.fraudRulesProperties = fraudRulesProperties;
    }

    @Operation(
        summary = "Evaluate a transaction and return fraud risk",
        description = "Calculates the fraud score, risk level, and matching rule reasons for a transaction request"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Fraud risk evaluation completed",
        content = @Content(schema = @Schema(implementation = FraudCheckResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request payload",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @PostMapping("/check")
    public FraudCheckResponse check(@Valid @RequestBody FraudCheckRequest request) {
        return fraudScoringService.evaluate(request);
    }

    @Operation(
        summary = "Get active fraud rules",
        description = "Returns scoring thresholds, rule points, configured high-risk countries, night window, and risk bands"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Fraud rules returned successfully",
        content = @Content(schema = @Schema(implementation = RulesResponse.class))
    )
    @GetMapping("/rules")
    public RulesResponse rules() {
        RulesResponse response = new RulesResponse();
        response.setAmountThresholds(Map.of(
            "moderateMin", 300,
            "highMin", 1000,
            "veryHighMin", 2000
        ));
        response.setPoints(Map.of(
            "amountModerate", 10,
            "amountHigh", 25,
            "amountVeryHigh", 35,
            "untrustedDevice", 20,
            "highRiskCountry", 25,
            "nightWindow", 10
        ));
        response.setHighRiskCountries(fraudRulesProperties.getHighRiskCountries());

        RulesResponse.NightWindowDto nightWindowDto = new RulesResponse.NightWindowDto();
        nightWindowDto.setStartHourUtc(fraudRulesProperties.getNightWindow().getStartHourUtc());
        nightWindowDto.setEndHourUtc(fraudRulesProperties.getNightWindow().getEndHourUtc());
        response.setNightWindow(nightWindowDto);

        response.setRiskBands(Map.of(
            "LOW", "0-29",
            "MEDIUM", "30-69",
            "HIGH", "70-100"
        ));

        return response;
    }
}
