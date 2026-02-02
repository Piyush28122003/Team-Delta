package com.yourorg.portfolio.controller;

import com.yourorg.portfolio.dto.PortfolioDTO;
import com.yourorg.portfolio.model.Investment;
import com.yourorg.portfolio.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "*")
public class PortfolioController {
    
    private final PortfolioService portfolioService;
    
    @Autowired
    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<PortfolioDTO> getPortfolioByUserId(@PathVariable Long userId) {
        PortfolioDTO portfolio = portfolioService.getPortfolioByUserId(userId);
        return ResponseEntity.ok(portfolio);
    }
    
    @PostMapping("/buy")
    public ResponseEntity<Investment> buyStock(
            @RequestParam Long userId,
            @RequestParam String symbol,
            @RequestParam Integer quantity,
            @RequestParam BigDecimal buyPrice) {
        Investment investment = portfolioService.buyStock(userId, symbol, quantity, buyPrice);
        return new ResponseEntity<>(investment, HttpStatus.CREATED);
    }
    
    @PostMapping("/sell")
    public ResponseEntity<Void> sellStock(
            @RequestParam Long userId,
            @RequestParam Long investmentId,
            @RequestParam Integer quantity) {
        portfolioService.sellStock(userId, investmentId, quantity);
        return ResponseEntity.ok().build();
    }
}

