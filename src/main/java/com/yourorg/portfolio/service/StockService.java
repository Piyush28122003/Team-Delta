package com.yourorg.portfolio.service;

import com.yourorg.portfolio.dto.StockPriceDTO;
import com.yourorg.portfolio.model.Stock;
import com.yourorg.portfolio.repository.StockRepository;
import com.yourorg.portfolio.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StockService {
    
    private static final Logger logger = LoggerFactory.getLogger(StockService.class);
    
    private final StockRepository stockRepository;
    private final StockApiClient stockApiClient;
    
    @Autowired
    public StockService(StockRepository stockRepository, StockApiClient stockApiClient) {
        this.stockRepository = stockRepository;
        this.stockApiClient = stockApiClient;
    }
    
    /**
     * Get stock by symbol
     */
    public Stock getStockBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol)
            .orElseThrow(() -> new ResourceNotFoundException("Stock", "symbol", symbol));
    }
    
    /**
     * Get or create stock by symbol
     */
    public Stock getOrCreateStock(String symbol) {
        Optional<Stock> stockOpt = stockRepository.findBySymbol(symbol);
        
        if (stockOpt.isPresent()) {
            return stockOpt.get();
        }
        
        // Fetch from API to get company name
        StockPriceDTO priceDTO = stockApiClient.fetchStockPrice(symbol);
        
        // Create new stock entity
        Stock stock = new Stock();
        stock.setSymbol(symbol);
        stock.setCompanyName(priceDTO.getCompanyName() != null ? 
            priceDTO.getCompanyName() : symbol + " Inc.");
        stock.setCurrency("USD");
        
        return stockRepository.save(stock);
    }
    
    /**
     * Search stocks by symbol or company name
     */
    public List<Stock> searchStocks(String query) {
        return stockRepository.searchBySymbolOrName(query);
    }
    
    /**
     * Get current stock price
     */
    public StockPriceDTO getCurrentPrice(String symbol) {
        return stockApiClient.fetchStockPrice(symbol);
    }
    
    /**
     * Get top trending stocks
     */
    public List<StockPriceDTO> getTrendingStocks() {
        return stockApiClient.fetchTrendingStocks();
    }
    
    /**
     * Get all stocks
     */
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }
}

