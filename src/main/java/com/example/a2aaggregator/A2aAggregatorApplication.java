package com.example.a2aaggregator;

import com.example.a2aaggregator.config.AggregatorProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties(AggregatorProperties.class)
public class A2aAggregatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(A2aAggregatorApplication.class, args);
    }
}
