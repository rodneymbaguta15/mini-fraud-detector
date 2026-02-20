package com.example.minifrauddetector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MiniFraudDetectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniFraudDetectorApplication.class, args);
    }
}
