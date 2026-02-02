package com.yourorg.portfolio.service;

import com.yourorg.portfolio.dto.StockPriceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StockApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(StockApiClient.class);
    
    private final WebClient webClient;
    
    @Value("${external.stock-api.provider:alpha-vantage}")
    private String provider;
    
    @Value("${external.stock-api.alpha-vantage.api-key:demo}")
    private String alphaVantageApiKey;
    
    @Value("${external.stock-api.finnhub.api-key:demo}")
    private String finnhubApiKey;
    
    public StockApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    /**
     * Fetch current stock price by symbol
     */
    public StockPriceDTO fetchStockPrice(String symbol) {
        try {
            return switch (provider) {
                case "alpha-vantage" -> fetchFromAlphaVantage(symbol);
                case "finnhub" -> fetchFromFinnhub(symbol);
                default -> fetchFromAlphaVantage(symbol);
            };
        } catch (Exception e) {
            logger.error("Error fetching stock price for symbol: {}", symbol, e);
            // Return mock data if API fails
            return createMockStockPrice(symbol);
        }
    }
    
    /**
     * Fetch top trending stocks
     */
    public List<StockPriceDTO> fetchTrendingStocks() {
        try {
            // For demo purposes, return popular stocks
            List<String> popularSymbols = List.of("AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", 
                                                   "META", "NVDA", "JPM", "V", "JNJ");
            List<StockPriceDTO> trending = new ArrayList<>();
            
            for (String symbol : popularSymbols) {
                StockPriceDTO price = fetchStockPrice(symbol);
                if (price != null) {
                    trending.add(price);
                }
            }
            
            return trending;
        } catch (Exception e) {
            logger.error("Error fetching trending stocks", e);
            return createMockTrendingStocks();
        }
    }
    
    /**
     * Fetch from Alpha Vantage API
     */
    private StockPriceDTO fetchFromAlphaVantage(String symbol) {
        try {
            String url = "https://www.alphavantage.co/query";
            
            Mono<Map<String, Object>> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(url)
                    .queryParam("function", "GLOBAL_QUOTE")
                    .queryParam("symbol", symbol)
                    .queryParam("apikey", alphaVantageApiKey)
                    .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
            
            Map<String, Object> result = response.block();
            
            if (result != null && result.containsKey("Global Quote")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> quoteMap = (Map<String, Object>) result.get("Global Quote");
                Map<String, String> quote = new HashMap<>();
                for (Map.Entry<String, Object> entry : quoteMap.entrySet()) {
                    quote.put(entry.getKey(), entry.getValue().toString());
                }
                
                if (quote != null && !quote.isEmpty()) {
                    StockPriceDTO dto = new StockPriceDTO();
                    dto.setSymbol(symbol);
                    dto.setCurrentPrice(new BigDecimal(quote.getOrDefault("05. price", "0")));
                    dto.setPreviousClose(new BigDecimal(quote.getOrDefault("08. previous close", "0")));
                    
                    BigDecimal change = dto.getCurrentPrice().subtract(dto.getPreviousClose());
                    dto.setChange(change);
                    
                    if (dto.getPreviousClose().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal changePercent = change
                            .divide(dto.getPreviousClose(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                        dto.setChangePercent(changePercent);
                    }
                    
                    dto.setTrend(change.compareTo(BigDecimal.ZERO) >= 0 ? "UP" : "DOWN");
                    dto.setCurrency("USD");
                    
                    return dto;
                }
            }
        } catch (Exception e) {
            logger.warn("Alpha Vantage API call failed, using mock data: {}", e.getMessage());
        }
        
        return createMockStockPrice(symbol);
    }
    
    /**
     * Fetch from Finnhub API
     */
    private StockPriceDTO fetchFromFinnhub(String symbol) {
        try {
            String url = "https://finnhub.io/api/v1/quote";
            
            Mono<Map<String, Object>> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(url)
                    .queryParam("symbol", symbol)
                    .queryParam("token", finnhubApiKey)
                    .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
            
            Map<String, Object> result = response.block();
            
            if (result != null) {
                StockPriceDTO dto = new StockPriceDTO();
                dto.setSymbol(symbol);
                
                Object currentPrice = result.get("c");
                Object previousClose = result.get("pc");
                
                if (currentPrice != null) {
                    dto.setCurrentPrice(new BigDecimal(currentPrice.toString()));
                }
                if (previousClose != null) {
                    dto.setPreviousClose(new BigDecimal(previousClose.toString()));
                }
                
                BigDecimal change = dto.getCurrentPrice().subtract(dto.getPreviousClose());
                dto.setChange(change);
                
                if (dto.getPreviousClose().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal changePercent = change
                        .divide(dto.getPreviousClose(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                    dto.setChangePercent(changePercent);
                }
                
                dto.setTrend(change.compareTo(BigDecimal.ZERO) >= 0 ? "UP" : "DOWN");
                dto.setCurrency("USD");
                
                return dto;
            }
        } catch (Exception e) {
            logger.warn("Finnhub API call failed, using mock data: {}", e.getMessage());
        }
        
        return createMockStockPrice(symbol);
    }
    
    /**
     * Create mock stock price for demo/testing
     */
    private StockPriceDTO createMockStockPrice(String symbol) {
        StockPriceDTO dto = new StockPriceDTO();
        dto.setSymbol(symbol);
        
        // Mock prices based on symbol
        Map<String, BigDecimal> mockPrices = new HashMap<>();
        mockPrices.put("AAPL", new BigDecimal("175.50"));
        mockPrices.put("GOOGL", new BigDecimal("142.30"));
        mockPrices.put("MSFT", new BigDecimal("385.20"));
        mockPrices.put("AMZN", new BigDecimal("145.80"));
        mockPrices.put("TSLA", new BigDecimal("245.60"));
        mockPrices.put("META", new BigDecimal("320.40"));
        mockPrices.put("NVDA", new BigDecimal("485.90"));
        mockPrices.put("JPM", new BigDecimal("155.20"));
        mockPrices.put("V", new BigDecimal("225.70"));
        mockPrices.put("JNJ", new BigDecimal("165.30"));
        mockPrices.put("WMT", new BigDecimal("152.40"));
        mockPrices.put("PG", new BigDecimal("145.60"));
        
        BigDecimal basePrice = mockPrices.getOrDefault(symbol, new BigDecimal("100.00"));
        BigDecimal previousClose = basePrice.multiply(new BigDecimal("0.98"));
        
        dto.setCurrentPrice(basePrice);
        dto.setPreviousClose(previousClose);
        
        BigDecimal change = basePrice.subtract(previousClose);
        dto.setChange(change);
        dto.setChangePercent(change.divide(previousClose, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100)));
        dto.setTrend(change.compareTo(BigDecimal.ZERO) >= 0 ? "UP" : "DOWN");
        dto.setCurrency("USD");
        
        return dto;
    }
    
    /**
     * Create mock trending stocks
     */
    private List<StockPriceDTO> createMockTrendingStocks() {
        List<String> symbols = List.of("AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", 
                                       "META", "NVDA", "JPM", "V", "JNJ");
        List<StockPriceDTO> trending = new ArrayList<>();
        
        for (String symbol : symbols) {
            trending.add(createMockStockPrice(symbol));
        }
        
        return trending;
    }
}

