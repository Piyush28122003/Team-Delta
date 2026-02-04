package com.yourorg.portfolio.service;

import com.yourorg.portfolio.dto.StockPriceDTO;
import com.yourorg.portfolio.exception.ResourceNotFoundException;
import com.yourorg.portfolio.model.*;
import com.yourorg.portfolio.repository.InvestmentRepository;
import com.yourorg.portfolio.repository.PortfolioRepository;
import com.yourorg.portfolio.repository.StockRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioService Unit Tests")
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockService stockService;

    @Mock
    private UserService userService;

    @InjectMocks
    private PortfolioService portfolioService;

    private User user;
    private Portfolio portfolio;
    private Stock stock;
    private Investment investment;
    private BankAccount bankAccount;

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
        portfolio.setName("John's Portfolio");

        stock = new Stock("AAPL", "Apple Inc.");
        stock.setId(1L);

        investment = new Investment();
        investment.setId(1L);
        investment.setPortfolio(portfolio);
        investment.setStock(stock);
        investment.setQuantity(10);
        investment.setBuyPrice(new BigDecimal("150.00"));
        investment.setBuyDate(LocalDate.now());

        bankAccount = new BankAccount();
        bankAccount.setUser(user);
        bankAccount.setCurrentBalance(new BigDecimal("50000"));
    }

    @Nested
    @DisplayName("getPortfolioByUserId")
    class GetPortfolioByUserIdTests {
        @Test
        void shouldThrowWhenPortfolioNotFound() {
            when(portfolioRepository.findByUserId(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> portfolioService.getPortfolioByUserId(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void shouldReturnPortfolioDTOWhenPortfolioExists() {
            when(portfolioRepository.findByUserId(1L)).thenReturn(Optional.of(portfolio));
            when(investmentRepository.findByPortfolioId(1L)).thenReturn(List.of(investment));
            when(userService.getOrCreateBankAccount(1L)).thenReturn(bankAccount);

            StockPriceDTO priceDTO = new StockPriceDTO();
            priceDTO.setSymbol("AAPL");
            priceDTO.setCurrentPrice(new BigDecimal("175.00"));
            when(stockService.getCurrentPrice("AAPL")).thenReturn(priceDTO);

            var result = portfolioService.getPortfolioByUserId(1L);

            assertThat(result).isNotNull();
            assertThat(result.getPortfolioId()).isEqualTo(1L);
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getHoldings()).hasSize(1);
            assertThat(result.getHoldings().get(0).getSymbol()).isEqualTo("AAPL");
            assertThat(result.getTotalValue()).isNotNull();
            assertThat(result.getTotalCost()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getOrCreatePortfolio")
    class GetOrCreatePortfolioTests {
        @Test
        void shouldReturnExistingPortfolioWhenFound() {
            when(portfolioRepository.findByUserId(1L)).thenReturn(Optional.of(portfolio));

            Portfolio result = portfolioService.getOrCreatePortfolio(user);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("John's Portfolio");
            verify(portfolioRepository, never()).save(any());
        }

        @Test
        void shouldCreateAndSaveNewPortfolioWhenNotFound() {
            when(portfolioRepository.findByUserId(1L)).thenReturn(Optional.empty());
            when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(inv -> {
                Portfolio p = inv.getArgument(0);
                p.setId(2L);
                return p;
            });

            Portfolio result = portfolioService.getOrCreatePortfolio(user);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("John's Portfolio");
            verify(portfolioRepository).save(any(Portfolio.class));
        }
    }

    @Nested
    @DisplayName("buyStock")
    class BuyStockTests {
        @Test
        void shouldBuyStockWhenSufficientBalance() {
            when(userService.getUserEntityById(1L)).thenReturn(user);
            when(portfolioRepository.findByUserId(1L)).thenReturn(Optional.of(portfolio));
            when(stockService.getOrCreateStock("AAPL")).thenReturn(stock);
            when(userService.getOrCreateBankAccount(1L)).thenReturn(bankAccount);
            when(investmentRepository.save(any(Investment.class))).thenAnswer(inv -> {
                Investment invArg = inv.getArgument(0);
                invArg.setId(1L);
                return invArg;
            });

            Investment result = portfolioService.buyStock(1L, "AAPL", 10, new BigDecimal("150.00"));

            assertThat(result).isNotNull();
            assertThat(result.getStock().getSymbol()).isEqualTo("AAPL");
            assertThat(result.getQuantity()).isEqualTo(10);
            assertThat(result.getBuyPrice()).isEqualByComparingTo(new BigDecimal("150.00"));
            verify(investmentRepository).save(any(Investment.class));
            verify(userService).updateBankBalance(eq(1L), eq(new BigDecimal("-1500.00")));
        }

        @Test
        void shouldThrowWhenInsufficientBankBalance() {
            bankAccount.setCurrentBalance(new BigDecimal("100"));
            when(userService.getUserEntityById(1L)).thenReturn(user);
            when(portfolioRepository.findByUserId(1L)).thenReturn(Optional.of(portfolio));
            when(stockService.getOrCreateStock("AAPL")).thenReturn(stock);
            when(userService.getOrCreateBankAccount(1L)).thenReturn(bankAccount);

            assertThatThrownBy(() -> portfolioService.buyStock(1L, "AAPL", 10, new BigDecimal("150.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Insufficient bank balance");
            verify(investmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("sellStock")
    class SellStockTests {
        @Test
        void shouldSellStockAndUpdateBankBalance() {
            when(investmentRepository.findById(1L)).thenReturn(Optional.of(investment));
            StockPriceDTO priceDTO = new StockPriceDTO();
            priceDTO.setCurrentPrice(new BigDecimal("175.00"));
            when(stockService.getCurrentPrice("AAPL")).thenReturn(priceDTO);

            portfolioService.sellStock(1L, 1L, 10);

            verify(investmentRepository).delete(investment);
            verify(userService).updateBankBalance(eq(1L), eq(new BigDecimal("1750.00")));
        }

        @Test
        void shouldThrowWhenInvestmentNotFound() {
            when(investmentRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> portfolioService.sellStock(1L, 999L, 5))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void shouldThrowWhenInvestmentDoesNotBelongToUser() {
            when(investmentRepository.findById(1L)).thenReturn(Optional.of(investment));

            assertThatThrownBy(() -> portfolioService.sellStock(999L, 1L, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Investment does not belong to user");
        }

        @Test
        void shouldThrowWhenSellingMoreThanOwned() {
            when(investmentRepository.findById(1L)).thenReturn(Optional.of(investment));

            assertThatThrownBy(() -> portfolioService.sellStock(1L, 1L, 20))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cannot sell more than owned");
        }
    }
}
