package com.yourorg.portfolio.service;

import com.yourorg.portfolio.dto.MarketIndexDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for market indices data (Sensex-style display).
 * Returns Indian and global indices with value, change, and trend.
 */
@Service
public class MarketIndicesService {

    public List<MarketIndexDTO> getMarketIndices() {
        List<MarketIndexDTO> indices = new ArrayList<>();
        long seed = System.currentTimeMillis() % 1000;

        // US stock exchange indices only
        indices.add(createIndex("S&P 500", "SPX", new BigDecimal("5821"), (seed + 1) % 2 == 0, 18, 0.31));
        indices.add(createIndex("NASDAQ", "NDX", new BigDecimal("18542"), seed % 4 != 0, 62, 0.34));
        indices.add(createIndex("Dow Jones", "DJI", new BigDecimal("39567"), (seed + 2) % 2 == 0, 95, 0.24));

        return indices;
    }

    private MarketIndexDTO createIndex(String name, String symbol, BigDecimal baseValue,
                                       boolean up, int changePoints, double changePercentBase) {
        BigDecimal change = up ? BigDecimal.valueOf(changePoints) : BigDecimal.valueOf(-changePoints);
        BigDecimal changePercent = BigDecimal.valueOf(changePercentBase * (up ? 1 : -1))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal value = baseValue.add(change);

        MarketIndexDTO dto = new MarketIndexDTO();
        dto.setName(name);
        dto.setSymbol(symbol);
        dto.setValue(value.setScale(2, RoundingMode.HALF_UP));
        dto.setChange(change.setScale(2, RoundingMode.HALF_UP));
        dto.setChangePercent(changePercent);
        dto.setTrend(up ? "UP" : "DOWN");
        return dto;
    }
}
