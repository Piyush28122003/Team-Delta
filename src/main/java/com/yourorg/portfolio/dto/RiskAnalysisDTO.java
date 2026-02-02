package com.yourorg.portfolio.dto;

import com.yourorg.portfolio.model.RiskProfile;
import java.math.BigDecimal;
import java.util.List;

public class RiskAnalysisDTO {
    
    private Long userId;
    private String userName;
    private RiskProfile.RiskCategory riskCategory;
    private BigDecimal volatilityScore;
    private BigDecimal diversificationScore;
    private BigDecimal maxLossTolerance;
    private RiskProfile.InvestmentHorizon investmentHorizon;
    private String riskLevel;
    private String recommendation;
    private List<String> riskFactors;
    private List<String> suggestions;
    
    public RiskAnalysisDTO() {}
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public RiskProfile.RiskCategory getRiskCategory() {
        return riskCategory;
    }
    
    public void setRiskCategory(RiskProfile.RiskCategory riskCategory) {
        this.riskCategory = riskCategory;
    }
    
    public BigDecimal getVolatilityScore() {
        return volatilityScore;
    }
    
    public void setVolatilityScore(BigDecimal volatilityScore) {
        this.volatilityScore = volatilityScore;
    }
    
    public BigDecimal getDiversificationScore() {
        return diversificationScore;
    }
    
    public void setDiversificationScore(BigDecimal diversificationScore) {
        this.diversificationScore = diversificationScore;
    }
    
    public BigDecimal getMaxLossTolerance() {
        return maxLossTolerance;
    }
    
    public void setMaxLossTolerance(BigDecimal maxLossTolerance) {
        this.maxLossTolerance = maxLossTolerance;
    }
    
    public RiskProfile.InvestmentHorizon getInvestmentHorizon() {
        return investmentHorizon;
    }
    
    public void setInvestmentHorizon(RiskProfile.InvestmentHorizon investmentHorizon) {
        this.investmentHorizon = investmentHorizon;
    }
    
    public String getRiskLevel() {
        return riskLevel;
    }
    
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
    
    public List<String> getRiskFactors() {
        return riskFactors;
    }
    
    public void setRiskFactors(List<String> riskFactors) {
        this.riskFactors = riskFactors;
    }
    
    public List<String> getSuggestions() {
        return suggestions;
    }
    
    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}

