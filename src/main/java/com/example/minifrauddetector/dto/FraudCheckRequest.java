package com.example.minifrauddetector.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;

@Data
public class FraudCheckRequest {

    @NotBlank
    private String transactionId;

    @NotBlank
    private String userId;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    private String merchantCategory;

    @NotBlank
    @Size(min = 2, max = 2)
    private String country;

    @NotNull
    private Instant timestamp;

    @NotNull
    private Boolean deviceTrusted;

    private String paymentMethod;

    private String ipAddress;
}
