package com.yourorg.portfolio.service;

import com.yourorg.portfolio.dto.ChatbotRequestDTO;
import com.yourorg.portfolio.dto.ChatbotResponseDTO;
import com.yourorg.portfolio.dto.PortfolioDTO;
import com.yourorg.portfolio.model.Investment;
import com.yourorg.portfolio.model.RiskProfile;
import com.yourorg.portfolio.repository.InvestmentRepository;
import com.yourorg.portfolio.repository.RiskProfileRepository;
import com.yourorg.portfolio.util.Constants;
import com.yourorg.portfolio.util.RecommendationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatbotService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);
    
    // Store user consent in memory (session-based)
    private final Map<Long, Boolean> userConsent = new ConcurrentHashMap<>();
    
    private final WebClient webClient;
    private final PortfolioService portfolioService;
    private final RiskAnalysisService riskAnalysisService;
    private final InvestmentRepository investmentRepository;
    private final RiskProfileRepository riskProfileRepository;
    private final UserService userService;
    private final StockService stockService;
    
    @Value("${external.gemini.api-key:your-gemini-api-key-here}")
    private String geminiApiKey;
    
    @Value("${external.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String geminiBaseUrl;
    
    @Value("${external.gemini.model:gemini-pro}")
    private String geminiModel;
    
    @Autowired
    public ChatbotService(WebClient.Builder webClientBuilder,
                         PortfolioService portfolioService,
                         RiskAnalysisService riskAnalysisService,
                         InvestmentRepository investmentRepository,
                         RiskProfileRepository riskProfileRepository,
                         UserService userService,
                         StockService stockService) {
        this.webClient = webClientBuilder.build();
        this.portfolioService = portfolioService;
        this.riskAnalysisService = riskAnalysisService;
        this.investmentRepository = investmentRepository;
        this.riskProfileRepository = riskProfileRepository;
        this.userService = userService;
        this.stockService = stockService;
    }
    
    /**
     * Process chatbot request
     */
    public ChatbotResponseDTO processMessage(ChatbotRequestDTO request) {
        Long userId = request.getUserId();
        String message = request.getMessage().toLowerCase().trim();
        Boolean consentGiven = request.getConsentGiven();
        
        // Check if consent is required
        if (!userConsent.containsKey(userId) || !userConsent.get(userId)) {
            if (consentGiven != null && consentGiven) {
                userConsent.put(userId, true);
                return createResponse(
                    "✅ Thank you! I now have permission to access your portfolio data. How can I help you today?",
                    false
                );
            } else {
                return createResponse(Constants.CHATBOT_CONSENT_MESSAGE, true);
            }
        }
        
        // Handle quick actions
        if (message.contains("recommend") || message.contains("buy") || message.contains("suggest stocks")) {
            return handleRecommendStocks(userId);
        }
        
        if (message.contains("sell") || message.contains("suggest sell")) {
            return handleRecommendSell(userId);
        }
        
        // Handle portfolio queries
        if (message.contains("portfolio value") || message.contains("total value") || message.contains("worth")) {
            return handlePortfolioValue(userId);
        }
        
        if (message.contains("risk") || message.contains("risky") || message.contains("volatility")) {
            return handleRiskAnalysis(userId);
        }
        
        if (message.contains("holdings") || message.contains("stocks") || message.contains("investments")) {
            return handleHoldings(userId);
        }
        
        // Default: Use Gemini API for natural language processing
        return handleNaturalLanguageQuery(userId, request.getMessage());
    }
    
    /**
     * Handle portfolio value query
     */
    private ChatbotResponseDTO handlePortfolioValue(Long userId) {
        try {
            PortfolioDTO portfolio = portfolioService.getPortfolioByUserId(userId);
            
            String response = String.format(
                "📊 **Your Portfolio Summary:**\n\n" +
                "💰 Total Value: $%,.2f\n" +
                "💵 Total Cost: $%,.2f\n" +
                "📈 Profit/Loss: $%,.2f (%.2f%%)\n\n" +
                "You have %d holdings in your portfolio.",
                portfolio.getTotalValue(),
                portfolio.getTotalCost(),
                portfolio.getTotalProfitLoss(),
                portfolio.getTotalProfitLossPercentage(),
                portfolio.getHoldings().size()
            );
            
            return createResponse(response, false);
        } catch (Exception e) {
            logger.error("Error getting portfolio value", e);
            return createResponse("Sorry, I couldn't retrieve your portfolio information. Please try again.", false);
        }
    }
    
    /**
     * Handle risk analysis query
     */
    private ChatbotResponseDTO handleRiskAnalysis(Long userId) {
        try {
            var riskAnalysis = riskAnalysisService.analyzeRisk(userId);
            
            String response = String.format(
                "⚠️ **Risk Analysis:**\n\n" +
                "Risk Category: %s\n" +
                "Volatility Score: %.2f/10\n" +
                "Diversification Score: %.2f/10\n" +
                "Max Loss Tolerance: %.2f%%\n\n" +
                "%s\n\n" +
                "**Recommendations:**\n%s",
                riskAnalysis.getRiskCategory(),
                riskAnalysis.getVolatilityScore(),
                riskAnalysis.getDiversificationScore(),
                riskAnalysis.getMaxLossTolerance(),
                riskAnalysis.getRiskLevel(),
                String.join("\n", riskAnalysis.getSuggestions())
            );
            
            return createResponse(response, false);
        } catch (Exception e) {
            logger.error("Error analyzing risk", e);
            return createResponse("Sorry, I couldn't analyze your risk profile. Please try again.", false);
        }
    }
    
    /**
     * Handle holdings query
     */
    private ChatbotResponseDTO handleHoldings(Long userId) {
        try {
            PortfolioDTO portfolio = portfolioService.getPortfolioByUserId(userId);
            
            StringBuilder response = new StringBuilder("📋 **Your Holdings:**\n\n");
            
            for (var holding : portfolio.getHoldings()) {
                response.append(String.format(
                    "• %s (%s): %d shares @ $%.2f\n" +
                    "  Current: $%.2f | P/L: $%.2f (%.2f%%)\n\n",
                    holding.getSymbol(),
                    holding.getCompanyName(),
                    holding.getQuantity(),
                    holding.getBuyPrice(),
                    holding.getCurrentPrice(),
                    holding.getProfitLoss(),
                    holding.getProfitLossPercentage()
                ));
            }
            
            return createResponse(response.toString(), false);
        } catch (Exception e) {
            logger.error("Error getting holdings", e);
            return createResponse("Sorry, I couldn't retrieve your holdings. Please try again.", false);
        }
    }
    
    /**
     * Handle stock recommendations
     */
    private ChatbotResponseDTO handleRecommendStocks(Long userId) {
        try {
            RiskProfile riskProfile = riskProfileRepository.findByUserId(userId).orElse(null);
            List<Investment> investments = investmentRepository.findByUserId(userId);
            
            List<String> recommendations = RecommendationEngine.recommendStocksToBuy(
                riskProfile, investments, null
            );
            
            String response = "📈 **Stock Recommendations to Buy:**\n\n" +
                String.join("\n", recommendations) + "\n\n" +
                RecommendationEngine.generateInvestmentAdvice(riskProfile);
            
            return createResponse(response, false);
        } catch (Exception e) {
            logger.error("Error generating recommendations", e);
            return createResponse("Sorry, I couldn't generate recommendations. Please try again.", false);
        }
    }
    
    /**
     * Handle sell recommendations
     */
    private ChatbotResponseDTO handleRecommendSell(Long userId) {
        try {
            RiskProfile riskProfile = riskProfileRepository.findByUserId(userId).orElse(null);
            List<Investment> investments = investmentRepository.findByUserId(userId);
            
            // Get current prices and generate recommendations
            List<String> recommendations = new ArrayList<>();
            for (Investment investment : investments) {
                var priceDTO = stockService.getCurrentPrice(investment.getStock().getSymbol());
                BigDecimal currentPrice = priceDTO != null ? priceDTO.getCurrentPrice() : investment.getBuyPrice();
                
                recommendations.addAll(RecommendationEngine.recommendStocksToSell(
                    riskProfile, List.of(investment), currentPrice, investment.getBuyPrice()
                ));
            }
            
            String response = "📉 **Stock Recommendations to Sell:**\n\n" +
                (recommendations.isEmpty() ? 
                    "✅ Your portfolio looks balanced. No immediate sell recommendations." :
                    String.join("\n", recommendations));
            
            return createResponse(response, false);
        } catch (Exception e) {
            logger.error("Error generating sell recommendations", e);
            return createResponse("Sorry, I couldn't generate sell recommendations. Please try again.", false);
        }
    }
    
    /**
     * Handle natural language queries using Gemini API
     */
    private ChatbotResponseDTO handleNaturalLanguageQuery(Long userId, String message) {
        try {
            // Build context from user's portfolio
            String context = buildPortfolioContext(userId);
            
            // Call Gemini API
            String prompt = String.format(
                "You are a helpful financial portfolio assistant. " +
                "User's portfolio context:\n%s\n\n" +
                "User question: %s\n\n" +
                "Provide a concise, helpful response (2-3 sentences max).",
                context,
                message
            );
            
            String geminiResponse = callGeminiAPI(prompt);
            
            return createResponse(geminiResponse, false);
        } catch (Exception e) {
            logger.error("Error calling Gemini API", e);
            return createResponse(
                "I'm having trouble processing that request. " +
                "Try asking about your portfolio value, risk analysis, or stock recommendations.",
                false
            );
        }
    }
    
    /**
     * Build portfolio context for Gemini
     */
    private String buildPortfolioContext(Long userId) {
        try {
            PortfolioDTO portfolio = portfolioService.getPortfolioByUserId(userId);
            var riskAnalysis = riskAnalysisService.analyzeRisk(userId);
            
            return String.format(
                "Portfolio Value: $%.2f, Holdings: %d, Risk Category: %s",
                portfolio.getTotalValue(),
                portfolio.getHoldings().size(),
                riskAnalysis.getRiskCategory()
            );
        } catch (Exception e) {
            return "Portfolio information unavailable";
        }
    }
    
    /**
     * Call Google Gemini API
     */
    private String callGeminiAPI(String prompt) {
        try {
            String url = geminiBaseUrl + "/models/" + geminiModel + ":generateContent";
            
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));
            
            Mono<Map<String, Object>> response = webClient.post()
                .uri(url + "?key=" + geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
            
            Map<String, Object> result = response.block();
            
            if (result != null && result.containsKey("candidates")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) result.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
                    if (contentMap != null) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            return (String) parts.get(0).get("text");
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Gemini API call failed, using fallback response: {}", e.getMessage());
        }
        
        // Fallback response
        return "I understand your question. For specific portfolio information, try asking about:\n" +
               "• Portfolio value\n" +
               "• Risk analysis\n" +
               "• Stock recommendations\n" +
               "• Holdings";
    }
    
    /**
     * Create chatbot response
     */
    private ChatbotResponseDTO createResponse(String response, boolean requiresConsent) {
        ChatbotResponseDTO dto = new ChatbotResponseDTO();
        dto.setResponse(response);
        dto.setRequiresConsent(requiresConsent);
        dto.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        if (!requiresConsent) {
            List<String> quickActions = List.of(
                "📈 Recommend Stocks to Buy",
                "📉 Suggest Stocks to Sell",
                "💰 Portfolio Value",
                "⚠️ Risk Analysis"
            );
            dto.setQuickActions(quickActions);
        }
        
        return dto;
    }
    
    /**
     * Clear user consent (for testing/logout)
     */
    public void clearConsent(Long userId) {
        userConsent.remove(userId);
    }
}

