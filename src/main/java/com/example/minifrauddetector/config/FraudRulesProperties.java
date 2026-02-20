package com.example.minifrauddetector.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "fraud")
public class FraudRulesProperties {

    private List<String> highRiskCountries = new ArrayList<>();
    private NightWindow nightWindow = new NightWindow();

    @Getter
    @Setter
    public static class NightWindow {

        private int startHourUtc = 0;
        private int endHourUtc = 5;
    }
}
