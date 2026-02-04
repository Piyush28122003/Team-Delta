package com.yourorg.portfolio.service;

import com.yourorg.portfolio.dto.ChatbotRequestDTO;
import com.yourorg.portfolio.dto.ChatbotResponseDTO;
import com.yourorg.portfolio.dto.PortfolioDTO;
import com.yourorg.portfolio.dto.RiskAnalysisDTO;
import com.yourorg.portfolio.model.RiskProfile;
import com.yourorg.portfolio.repository.InvestmentRepository;
import com.yourorg.portfolio.repository.RiskProfileRepository;
import com.yourorg.portfolio.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ChatbotService Unit Tests")
class ChatbotServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private RiskAnalysisService riskAnalysisService;

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private RiskProfileRepository riskProfileRepository;

    @Mock
    private UserService userService;

    @Mock
    private StockService stockService;

    @InjectMocks
    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Nested
    @DisplayName("processMessage - consent")
    class ConsentTests {
        @Test
        void shouldReturnConsentMessageWhenConsentNotGiven() {
            ChatbotRequestDTO request = new ChatbotRequestDTO(1L, "hello", false);

            ChatbotResponseDTO result = chatbotService.processMessage(request);

            assertThat(result).isNotNull();
            assertThat(result.getResponse()).isEqualTo(Constants.CHATBOT_CONSENT_MESSAGE);
            assertThat(result.isRequiresConsent()).isTrue();
        }

        @Test
        void shouldAcceptConsentAndReturnWelcomeWhenConsentGiven() {
            ChatbotRequestDTO request = new ChatbotRequestDTO(1L, "hello", true);

            ChatbotResponseDTO result = chatbotService.processMessage(request);

            assertThat(result).isNotNull();
            assertThat(result.getResponse()).contains("Thank you");
            assertThat(result.isRequiresConsent()).isFalse();
            assertThat(result.getQuickActions()).isNotEmpty();
        }

        @Test
        void shouldNotRequireConsentAfterConsentGiven() {
            chatbotService.processMessage(new ChatbotRequestDTO(1L, "hello", true));
            PortfolioDTO portfolio = new PortfolioDTO();
            portfolio.setTotalValue(BigDecimal.ZERO);
            portfolio.setTotalCost(BigDecimal.ZERO);
            portfolio.setTotalProfitLoss(BigDecimal.ZERO);
            portfolio.setTotalProfitLossPercentage(BigDecimal.ZERO);
            portfolio.setHoldings(List.of());
            when(portfolioService.getPortfolioByUserId(1L)).thenReturn(portfolio);

            ChatbotRequestDTO followUp = new ChatbotRequestDTO(1L, "portfolio value", null);
            ChatbotResponseDTO result = chatbotService.processMessage(followUp);

            assertThat(result).isNotNull();
            assertThat(result.isRequiresConsent()).isFalse();
        }
    }

    @Nested
    @DisplayName("processMessage - portfolio value")
    class PortfolioValueTests {
        @BeforeEach
        void giveConsent() {
            chatbotService.processMessage(new ChatbotRequestDTO(1L, "consent", true));
        }

        @Test
        void shouldReturnPortfolioValueWhenUserAsks() {
            PortfolioDTO portfolio = new PortfolioDTO();
            portfolio.setTotalValue(new BigDecimal("10000"));
            portfolio.setTotalCost(new BigDecimal("9000"));
            portfolio.setTotalProfitLoss(new BigDecimal("1000"));
            portfolio.setTotalProfitLossPercentage(new BigDecimal("11.11"));
            portfolio.setHoldings(List.of());

            when(portfolioService.getPortfolioByUserId(1L)).thenReturn(portfolio);

            ChatbotRequestDTO request = new ChatbotRequestDTO(1L, "what is my portfolio value?", null);
            ChatbotResponseDTO result = chatbotService.processMessage(request);

            assertThat(result).isNotNull();
            assertThat(result.getResponse()).contains("10,000");
            assertThat(result.getResponse()).contains("Portfolio Summary");
        }

        @Test
        void shouldReturnErrorMessageWhenPortfolioServiceFails() {
            when(portfolioService.getPortfolioByUserId(1L))
                    .thenThrow(new RuntimeException("Portfolio not found"));

            ChatbotRequestDTO request = new ChatbotRequestDTO(1L, "portfolio value", null);
            ChatbotResponseDTO result = chatbotService.processMessage(request);

            assertThat(result).isNotNull();
            assertThat(result.getResponse()).contains("Sorry");
        }
    }

    @Nested
    @DisplayName("processMessage - risk analysis")
    class RiskAnalysisTests {
        @BeforeEach
        void giveConsent() {
            chatbotService.processMessage(new ChatbotRequestDTO(1L, "consent", true));
        }

        @Test
        void shouldReturnRiskAnalysisWhenUserAsks() {
            RiskAnalysisDTO riskDTO = new RiskAnalysisDTO();
            riskDTO.setUserId(1L);
            riskDTO.setRiskCategory(RiskProfile.RiskCategory.MODERATE);
            riskDTO.setVolatilityScore(new BigDecimal("5.0"));
            riskDTO.setDiversificationScore(new BigDecimal("6.0"));
            riskDTO.setMaxLossTolerance(new BigDecimal("15.0"));
            riskDTO.setRiskLevel("Medium Risk");
            riskDTO.setSuggestions(List.of("Diversify your portfolio"));

            when(riskAnalysisService.analyzeRisk(1L)).thenReturn(riskDTO);

            ChatbotRequestDTO request = new ChatbotRequestDTO(1L, "what is my risk?", null);
            ChatbotResponseDTO result = chatbotService.processMessage(request);

            assertThat(result).isNotNull();
            assertThat(result.getResponse()).contains("Risk Analysis");
            assertThat(result.getResponse()).contains("MODERATE");
        }
    }

    @Nested
    @DisplayName("processMessage - recommend stocks")
    class RecommendStocksTests {
        @BeforeEach
        void giveConsent() {
            chatbotService.processMessage(new ChatbotRequestDTO(1L, "consent", true));
        }

        @Test
        void shouldReturnRecommendationsWhenUserAsksToRecommend() {
            when(riskProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
            when(investmentRepository.findByUserId(1L)).thenReturn(List.of());

            ChatbotRequestDTO request = new ChatbotRequestDTO(1L, "recommend stocks to buy", null);
            ChatbotResponseDTO result = chatbotService.processMessage(request);

            assertThat(result).isNotNull();
            assertThat(result.getResponse()).contains("Recommendations");
        }
    }

    @Nested
    @DisplayName("clearConsent")
    class ClearConsentTests {
        @Test
        void shouldClearConsentForUser() {
            chatbotService.processMessage(new ChatbotRequestDTO(1L, "consent", true));
            chatbotService.clearConsent(1L);

            ChatbotRequestDTO request = new ChatbotRequestDTO(1L, "portfolio value", null);
            ChatbotResponseDTO result = chatbotService.processMessage(request);

            assertThat(result).isNotNull();
            assertThat(result.isRequiresConsent()).isTrue();
            assertThat(result.getResponse()).isEqualTo(Constants.CHATBOT_CONSENT_MESSAGE);
        }
    }
}
