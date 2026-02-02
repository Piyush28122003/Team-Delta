package com.yourorg.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PortfolioManagerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PortfolioManagerApplication.class, args);
    }
}

