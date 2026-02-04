package com.yourorg.portfolio.service;

import com.yourorg.portfolio.dto.RiskAnalysisDTO;
import com.yourorg.portfolio.exception.ResourceNotFoundException;
import com.yourorg.portfolio.model.*;
import com.yourorg.portfolio.repository.InvestmentRepository;
import com.yourorg.portfolio.repository.RiskProfileRepository;
import com.yourorg.portfolio.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RiskAnalysisService Unit Tests")
class RiskAnalysisServiceTest {

    @Mock
    private RiskProfileRepository riskProfileRepository;

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RiskAnalysisService riskAnalysisService;

    private User user;
    private RiskProfile riskProfile;
    private Investment investment;
    private Stock stock;
    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("johndoe");
        user.setFirstName("John");
        user.setLastName("Doe");

        portfolio = new Portfolio();
        portfolio.setId(1L);
        portfolio.setUser(user);

        stock = new Stock("AAPL", "Apple Inc.");
        stock.setId(1L);
        stock.setSector("Healthcare");
        stock.setIndustry("Pharma");

        investment = new Investment();
        investment.setId(1L);
        investment.setPortfolio(portfolio);
        investment.setStock(stock);
        investment.setQuantity(10);
        investment.setBuyPrice(new BigDecimal("150.00"));
        investment.setBuyDate(LocalDate.now());

        riskProfile = new RiskProfile();
        riskProfile.setId(1L);
        riskProfile.setUser(user);
        riskProfile.setRiskCategory(RiskProfile.RiskCategory.MODERATE);
        riskProfile.setVolatilityScore(new BigDecimal("5.0"));
        riskProfile.setDiversificationScore(new BigDecimal("6.0"));
        riskProfile.setMaxLossTolerance(new BigDecimal("15.0"));
    }

    @Nested
    @DisplayName("analyzeRisk")
    class AnalyzeRiskTests {
        @Test
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> riskAnalysisService.analyzeRisk(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void shouldAnalyzeRiskAndReturnDTOWhenUserExistsWithExistingProfile() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(investmentRepository.findByUserId(1L)).thenReturn(List.of(investment));
            when(riskProfileRepository.findByUserId(1L)).thenReturn(Optional.of(riskProfile));
            when(riskProfileRepository.save(any(RiskProfile.class))).thenReturn(riskProfile);

            RiskAnalysisDTO result = riskAnalysisService.analyzeRisk(1L);

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getUserName()).isEqualTo("John Doe");
            assertThat(result.getRiskCategory()).isIn(RiskProfile.RiskCategory.CONSERVATIVE,
                    RiskProfile.RiskCategory.MODERATE, RiskProfile.RiskCategory.AGGRESSIVE);
            assertThat(result.getVolatilityScore()).isNotNull();
            assertThat(result.getDiversificationScore()).isNotNull();
            assertThat(result.getMaxLossTolerance()).isNotNull();
            assertThat(result.getRiskLevel()).isNotNull();
            assertThat(result.getRecommendation()).isNotNull();
            assertThat(result.getRiskFactors()).isNotNull();
            assertThat(result.getSuggestions()).isNotNull();
            verify(riskProfileRepository).save(any(RiskProfile.class));
        }

        @Test
        void shouldCreateNewRiskProfileWhenNoneExists() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(investmentRepository.findByUserId(1L)).thenReturn(List.of(investment));
            when(riskProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
            when(riskProfileRepository.save(any(RiskProfile.class))).thenAnswer(inv -> {
                RiskProfile p = inv.getArgument(0);
                p.setId(2L);
                return p;
            });

            RiskAnalysisDTO result = riskAnalysisService.analyzeRisk(1L);

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getRiskCategory()).isNotNull();
            verify(riskProfileRepository, atLeastOnce()).save(any(RiskProfile.class));
        }

        @Test
        void shouldReturnConservativeCategoryForEmptyPortfolio() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(investmentRepository.findByUserId(1L)).thenReturn(List.of());
            when(riskProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
            when(riskProfileRepository.save(any(RiskProfile.class))).thenAnswer(inv -> inv.getArgument(0));

            RiskAnalysisDTO result = riskAnalysisService.analyzeRisk(1L);

            assertThat(result).isNotNull();
            assertThat(result.getRiskCategory()).isEqualTo(RiskProfile.RiskCategory.CONSERVATIVE);
            assertThat(result.getVolatilityScore()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getDiversificationScore()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}
