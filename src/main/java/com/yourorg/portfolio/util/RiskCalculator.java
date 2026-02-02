package com.yourorg.portfolio.util;

import com.yourorg.portfolio.model.Investment;
import com.yourorg.portfolio.model.RiskProfile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RiskCalculator {
    
    private static final BigDecimal HIGH_VOLATILITY_THRESHOLD = new BigDecimal("7.0");
    private static final BigDecimal MODERATE_VOLATILITY_THRESHOLD = new BigDecimal("4.0");
    private static final BigDecimal HIGH_DIVERSIFICATION_THRESHOLD = new BigDecimal("7.0");
    private static final BigDecimal MODERATE_DIVERSIFICATION_THRESHOLD = new BigDecimal("5.0");
    
    /**
     * Calculate volatility score based on portfolio holdings
     * Higher score = higher risk
     */
    public static BigDecimal calculateVolatilityScore(List<Investment> investments) {
        if (investments == null || investments.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Simplified volatility calculation
        // In real scenario, this would use historical price data
        int techStockCount = 0;
        int totalStocks = investments.size();
        
        for (Investment investment : investments) {
            String sector = investment.getStock().getSector();
            if (sector != null && sector.toLowerCase().contains("technology")) {
                techStockCount++;
            }
        }
        
        // Tech stocks are generally more volatile
        BigDecimal techRatio = BigDecimal.valueOf(techStockCount)
            .divide(BigDecimal.valueOf(totalStocks), 2, RoundingMode.HALF_UP);
        
        // Base volatility score (0-10 scale)
        BigDecimal baseScore = techRatio.multiply(BigDecimal.valueOf(8))
            .add(BigDecimal.valueOf(2));
        
        return baseScore.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate diversification score based on portfolio holdings
     * Higher score = better diversification
     */
    public static BigDecimal calculateDiversificationScore(List<Investment> investments) {
        if (investments == null || investments.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Count unique sectors
        Set<String> sectors = investments.stream()
            .map(inv -> inv.getStock().getSector())
            .filter(sector -> sector != null && !sector.isEmpty())
            .collect(Collectors.toSet());
        
        // Count unique industries
        Set<String> industries = investments.stream()
            .map(inv -> inv.getStock().getIndustry())
            .filter(industry -> industry != null && !industry.isEmpty())
            .collect(Collectors.toSet());
        
        int totalInvestments = investments.size();
        
        // Diversification score based on:
        // - Number of unique sectors (max 10 points)
        // - Number of unique industries (max 5 points)
        // - Total number of holdings (max 5 points, capped)
        BigDecimal sectorScore = BigDecimal.valueOf(Math.min(sectors.size() * 2, 10));
        BigDecimal industryScore = BigDecimal.valueOf(Math.min(industries.size(), 5));
        BigDecimal holdingsScore = BigDecimal.valueOf(Math.min(totalInvestments, 5));
        
        BigDecimal totalScore = sectorScore.add(industryScore).add(holdingsScore);
        
        // Normalize to 0-10 scale
        return totalScore.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)
            .min(BigDecimal.TEN);
    }
    
    /**
     * Determine risk category based on volatility and diversification scores
     */
    public static RiskProfile.RiskCategory determineRiskCategory(
            BigDecimal volatilityScore, 
            BigDecimal diversificationScore) {
        
        if (volatilityScore.compareTo(HIGH_VOLATILITY_THRESHOLD) > 0) {
            return RiskProfile.RiskCategory.AGGRESSIVE;
        } else if (volatilityScore.compareTo(MODERATE_VOLATILITY_THRESHOLD) > 0) {
            return RiskProfile.RiskCategory.MODERATE;
        } else {
            return RiskProfile.RiskCategory.CONSERVATIVE;
        }
    }
    
    /**
     * Calculate maximum loss tolerance based on risk category
     */
    public static BigDecimal calculateMaxLossTolerance(RiskProfile.RiskCategory riskCategory) {
        return switch (riskCategory) {
            case CONSERVATIVE -> new BigDecimal("5.0");
            case MODERATE -> new BigDecimal("15.0");
            case AGGRESSIVE -> new BigDecimal("25.0");
        };
    }
    
    /**
     * Get risk level description
     */
    public static String getRiskLevelDescription(RiskProfile.RiskCategory riskCategory) {
        return switch (riskCategory) {
            case CONSERVATIVE -> "Low Risk - Focus on capital preservation";
            case MODERATE -> "Medium Risk - Balanced growth and stability";
            case AGGRESSIVE -> "High Risk - Focus on aggressive growth";
        };
    }
}

