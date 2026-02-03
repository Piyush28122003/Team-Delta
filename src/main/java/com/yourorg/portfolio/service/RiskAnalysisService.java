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
        dto.setRiskLevelPlain(RiskCalculator.getRiskLevelPlainDescription(riskProfile.getRiskCategory()));
        
        // Generate recommendations
        dto.setRecommendation(generateRecommendation(riskProfile, investments));
        dto.setRiskFactors(identifyRiskFactors(riskProfile, investments));
        dto.setSuggestions(generateSuggestions(riskProfile, investments));
        
        return dto;
    }
    
    /**
     * Generate risk recommendation in plain language for lay users
     */
    private String generateRecommendation(RiskProfile riskProfile, List<Investment> investments) {
        StringBuilder recommendation = new StringBuilder();
        
        switch (riskProfile.getRiskCategory()) {
            case CONSERVATIVE:
                recommendation.append("Your investments are set up in a cautious, safe way — which is great if you want to protect your money. ");
                if (riskProfile.getDiversificationScore().compareTo(new BigDecimal("5.0")) < 0) {
                    recommendation.append("However, you're putting most of your eggs in few baskets. Spreading your money across different types of industries (like tech, healthcare, finance) can make your portfolio safer over time.");
                } else {
                    recommendation.append("Keep doing what you're doing — your strategy fits well with your goal of preserving your capital.");
                }
                break;
                
            case MODERATE:
                recommendation.append("Your portfolio strikes a middle ground between growth and stability — you're willing to take some risk for better returns. ");
                if (riskProfile.getVolatilityScore().compareTo(new BigDecimal("6.0")) > 0) {
                    recommendation.append("Your investments may swing up and down more than average. Adding a few steady, stable stocks could help smooth out the bumps.");
                } else {
                    recommendation.append("You have a good balance — your mix of investments looks solid.");
                }
                break;
                
            case AGGRESSIVE:
                recommendation.append("Your portfolio is geared for higher growth, which means you're comfortable with bigger ups and downs. ");
                recommendation.append("Keep an eye on your investments regularly, as they may change in value more often. ");
                if (investments.size() < 5) {
                    recommendation.append("Spreading your money across more different stocks or funds can help reduce risk if one or two don't perform well.");
                }
                break;
        }
        
        return recommendation.toString();
    }
    
    /**
     * Identify risk factors in plain language for lay users
     */
    private List<String> identifyRiskFactors(RiskProfile riskProfile, List<Investment> investments) {
        List<String> factors = new ArrayList<>();
        
        if (riskProfile.getVolatilityScore().compareTo(new BigDecimal("7.0")) > 0) {
            factors.add("Your portfolio value tends to swing up and down a lot — this means you could see bigger gains, but also bigger drops. It's something to be aware of and prepare for emotionally.");
        }
        
        if (riskProfile.getDiversificationScore().compareTo(new BigDecimal("5.0")) < 0) {
            factors.add("Your money is concentrated in just a few industries. If one of those industries hits a rough patch, it could affect a large part of your portfolio. Spreading across more sectors helps cushion the blow.");
        }
        
        if (investments.size() < 3) {
            factors.add("You own only a small number of stocks or funds. If even one performs poorly, it could significantly impact your overall portfolio. Adding more holdings can spread out that risk.");
        }
        
        if (factors.isEmpty()) {
            factors.add("We didn't spot any major concerns. Your portfolio looks well-structured for your risk level.");
        }
        
        return factors;
    }
    
    /**
     * Generate suggestions in plain language for lay users
     */
    private List<String> generateSuggestions(RiskProfile riskProfile, List<Investment> investments) {
        List<String> suggestions = new ArrayList<>();
        
        if (riskProfile.getDiversificationScore().compareTo(new BigDecimal("6.0")) < 0) {
            suggestions.add("💡 <strong>Spread your investments across at least 5 different sectors</strong> — For example: technology, healthcare, finance, consumer goods, and energy. This way, if one industry struggles, the others can help balance it out.");
            suggestions.add("💡 <strong>Consider adding bonds or dividend-paying stocks</strong> — Bonds are generally steadier than stocks and pay regular interest. Dividend stocks are shares in companies that regularly share profits with investors — both can add stability to your portfolio.");
        }
        
        if (riskProfile.getVolatilityScore().compareTo(new BigDecimal("7.0")) > 0) {
            suggestions.add("⚠️ <strong>Add some defensive or stable stocks</strong> — These are companies (like utilities or consumer staples) whose value tends to hold up better when the market dips. They can help smooth out the ups and downs.");
            suggestions.add("⚠️ <strong>Keep 10–20% of your money in cash</strong> — Having cash on hand lets you take advantage of dips without selling at a loss, and gives you peace of mind during market volatility.");
        }
        
        if (investments.size() < 5) {
            suggestions.add("📊 <strong>Add more holdings to reduce concentration risk</strong> — Instead of having a lot of money in just a few stocks, consider investing in more companies or funds. Think of it as not putting all your eggs in one basket.");
        }
        
        switch (riskProfile.getRiskCategory()) {
            case CONSERVATIVE:
                suggestions.add("✅ <strong>You're on the right track</strong> — Your cautious approach matches your goal of protecting your money. Keep focusing on steady, reliable investments.");
                break;
            case MODERATE:
                suggestions.add("✅ <strong>Your mix looks good</strong> — You're balancing growth potential with stability, which suits someone comfortable with moderate risk.");
                break;
            case AGGRESSIVE:
                suggestions.add("⚡ <strong>Check in on your portfolio regularly</strong> — With a higher-risk strategy, values can change quickly. A quick monthly or quarterly review can help you stay on top of things.");
                suggestions.add("⚡ <strong>Consider rebalancing every few months</strong> — If some investments grow a lot, selling a small amount and buying others can keep your risk level in line with your goals.");
                break;
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("✅ <strong>Your portfolio is well-balanced</strong> — No major changes needed. Keep up the good work!");
        }
        
        return suggestions;
    }
}

