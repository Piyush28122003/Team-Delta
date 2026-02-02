package com.yourorg.portfolio.service;

import com.yourorg.portfolio.dto.RiskAnalysisDTO;
import com.yourorg.portfolio.model.Investment;
import com.yourorg.portfolio.model.RiskProfile;
import com.yourorg.portfolio.model.User;
import com.yourorg.portfolio.repository.InvestmentRepository;
import com.yourorg.portfolio.repository.RiskProfileRepository;
import com.yourorg.portfolio.repository.UserRepository;
import com.yourorg.portfolio.util.RiskCalculator;
import com.yourorg.portfolio.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RiskAnalysisService {
    
    private final RiskProfileRepository riskProfileRepository;
    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public RiskAnalysisService(RiskProfileRepository riskProfileRepository,
                               InvestmentRepository investmentRepository,
                               UserRepository userRepository) {
        this.riskProfileRepository = riskProfileRepository;
        this.investmentRepository = investmentRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Analyze risk for a user
     */
    public RiskAnalysisDTO analyzeRisk(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        List<Investment> investments = investmentRepository.findByUserId(userId);
        
        // Calculate scores
        BigDecimal volatilityScore = RiskCalculator.calculateVolatilityScore(investments);
        BigDecimal diversificationScore = RiskCalculator.calculateDiversificationScore(investments);
        
        // Get or create risk profile
        RiskProfile riskProfile = riskProfileRepository.findByUserId(userId)
            .orElseGet(() -> {
                RiskProfile.RiskCategory category = RiskCalculator.determineRiskCategory(
                    volatilityScore, diversificationScore
                );
                RiskProfile newProfile = new RiskProfile();
                newProfile.setUser(user);
                newProfile.setRiskCategory(category);
                newProfile.setVolatilityScore(volatilityScore);
                newProfile.setDiversificationScore(diversificationScore);
                newProfile.setMaxLossTolerance(
                    RiskCalculator.calculateMaxLossTolerance(category)
                );
                return riskProfileRepository.save(newProfile);
            });
        
        // Update risk profile with latest scores
        riskProfile.setVolatilityScore(volatilityScore);
        riskProfile.setDiversificationScore(diversificationScore);
        riskProfile.setRiskCategory(
            RiskCalculator.determineRiskCategory(volatilityScore, diversificationScore)
        );
        riskProfile.setMaxLossTolerance(
            RiskCalculator.calculateMaxLossTolerance(riskProfile.getRiskCategory())
        );
        riskProfile = riskProfileRepository.save(riskProfile);
        
        // Build DTO
        RiskAnalysisDTO dto = new RiskAnalysisDTO();
        dto.setUserId(userId);
        dto.setUserName(user.getFirstName() + " " + user.getLastName());
        dto.setRiskCategory(riskProfile.getRiskCategory());
        dto.setVolatilityScore(riskProfile.getVolatilityScore());
        dto.setDiversificationScore(riskProfile.getDiversificationScore());
        dto.setMaxLossTolerance(riskProfile.getMaxLossTolerance());
        dto.setInvestmentHorizon(riskProfile.getInvestmentHorizon());
        dto.setRiskLevel(RiskCalculator.getRiskLevelDescription(riskProfile.getRiskCategory()));
        
        // Generate recommendations
        dto.setRecommendation(generateRecommendation(riskProfile, investments));
        dto.setRiskFactors(identifyRiskFactors(riskProfile, investments));
        dto.setSuggestions(generateSuggestions(riskProfile, investments));
        
        return dto;
    }
    
    /**
     * Generate risk recommendation
     */
    private String generateRecommendation(RiskProfile riskProfile, List<Investment> investments) {
        StringBuilder recommendation = new StringBuilder();
        
        switch (riskProfile.getRiskCategory()) {
            case CONSERVATIVE:
                recommendation.append("Your portfolio is conservative. ");
                if (riskProfile.getDiversificationScore().compareTo(new BigDecimal("5.0")) < 0) {
                    recommendation.append("Consider diversifying across more sectors.");
                } else {
                    recommendation.append("Maintain current strategy for capital preservation.");
                }
                break;
                
            case MODERATE:
                recommendation.append("Your portfolio has moderate risk. ");
                if (riskProfile.getVolatilityScore().compareTo(new BigDecimal("6.0")) > 0) {
                    recommendation.append("Consider adding some stable stocks to balance volatility.");
                } else {
                    recommendation.append("Good balance between growth and stability.");
                }
                break;
                
            case AGGRESSIVE:
                recommendation.append("Your portfolio is aggressive. ");
                recommendation.append("Monitor closely and be prepared for higher volatility. ");
                if (investments.size() < 5) {
                    recommendation.append("Consider diversifying to reduce concentration risk.");
                }
                break;
        }
        
        return recommendation.toString();
    }
    
    /**
     * Identify risk factors
     */
    private List<String> identifyRiskFactors(RiskProfile riskProfile, List<Investment> investments) {
        List<String> factors = new ArrayList<>();
        
        if (riskProfile.getVolatilityScore().compareTo(new BigDecimal("7.0")) > 0) {
            factors.add("High volatility detected in portfolio");
        }
        
        if (riskProfile.getDiversificationScore().compareTo(new BigDecimal("5.0")) < 0) {
            factors.add("Low diversification - concentrated in few sectors");
        }
        
        if (investments.size() < 3) {
            factors.add("Limited number of holdings - concentration risk");
        }
        
        if (factors.isEmpty()) {
            factors.add("No significant risk factors identified");
        }
        
        return factors;
    }
    
    /**
     * Generate suggestions
     */
    private List<String> generateSuggestions(RiskProfile riskProfile, List<Investment> investments) {
        List<String> suggestions = new ArrayList<>();
        
        if (riskProfile.getDiversificationScore().compareTo(new BigDecimal("6.0")) < 0) {
            suggestions.add("💡 Diversify across at least 5 different sectors");
            suggestions.add("💡 Consider adding bonds or stable dividend stocks");
        }
        
        if (riskProfile.getVolatilityScore().compareTo(new BigDecimal("7.0")) > 0) {
            suggestions.add("⚠️ High volatility - consider adding defensive stocks");
            suggestions.add("⚠️ Maintain adequate cash reserves (10-20%)");
        }
        
        if (investments.size() < 5) {
            suggestions.add("📊 Add more holdings to reduce concentration risk");
        }
        
        switch (riskProfile.getRiskCategory()) {
            case CONSERVATIVE:
                suggestions.add("✅ Your conservative approach aligns with capital preservation goals");
                break;
            case MODERATE:
                suggestions.add("✅ Balanced portfolio suitable for moderate risk tolerance");
                break;
            case AGGRESSIVE:
                suggestions.add("⚡ Monitor portfolio closely due to high-risk strategy");
                suggestions.add("⚡ Consider rebalancing quarterly");
                break;
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("✅ Your portfolio is well-balanced");
        }
        
        return suggestions;
    }
}

