package com.example.minifrauddetector.controller;

import com.example.minifrauddetector.dto.FraudCheckRequest;
import com.example.minifrauddetector.dto.FraudCheckResponse;
import com.example.minifrauddetector.service.FraudScoringService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fraud")
public class FraudController {

    private final FraudScoringService fraudScoringService;

    public FraudController(FraudScoringService fraudScoringService) {
        this.fraudScoringService = fraudScoringService;
    }

    @PostMapping("/check")
    public FraudCheckResponse check(@Valid @RequestBody FraudCheckRequest request) {
        return fraudScoringService.evaluate(request);
    }
}
