package com.yourorg.portfolio.controller;

import com.yourorg.portfolio.dto.StockPriceDTO;
import com.yourorg.portfolio.model.Stock;
import com.yourorg.portfolio.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = "*")
public class StockController {
    
    private final StockService stockService;
    
    @Autowired
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }
    
    @GetMapping("/price/{symbol}")
    public ResponseEntity<StockPriceDTO> getStockPrice(@PathVariable String symbol) {
        StockPriceDTO price = stockService.getCurrentPrice(symbol);
        return ResponseEntity.ok(price);
    }
    
    @GetMapping("/trending")
    public ResponseEntity<List<StockPriceDTO>> getTrendingStocks() {
        List<StockPriceDTO> trending = stockService.getTrendingStocks();
        return ResponseEntity.ok(trending);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Stock>> searchStocks(@RequestParam String query) {
        List<Stock> stocks = stockService.searchStocks(query);
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping
    public ResponseEntity<List<Stock>> getAllStocks() {
        List<Stock> stocks = stockService.getAllStocks();
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/{symbol}")
    public ResponseEntity<Stock> getStockBySymbol(@PathVariable String symbol) {
        Stock stock = stockService.getStockBySymbol(symbol);
        return ResponseEntity.ok(stock);
    }
}

