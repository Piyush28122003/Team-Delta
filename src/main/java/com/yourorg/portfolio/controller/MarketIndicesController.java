package com.yourorg.portfolio.controller;

import com.yourorg.portfolio.dto.MarketIndexDTO;
import com.yourorg.portfolio.service.MarketIndicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market-indices")
@CrossOrigin(origins = "*")
public class MarketIndicesController {

    private final MarketIndicesService marketIndicesService;

    @Autowired
    public MarketIndicesController(MarketIndicesService marketIndicesService) {
        this.marketIndicesService = marketIndicesService;
    }

    @GetMapping
    public ResponseEntity<List<MarketIndexDTO>> getMarketIndices() {
        return ResponseEntity.ok(marketIndicesService.getMarketIndices());
    }
}
