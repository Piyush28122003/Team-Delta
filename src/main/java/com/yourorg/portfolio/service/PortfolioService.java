package com.yourorg.portfolio.service;

import com.yourorg.portfolio.dto.PortfolioDTO;
import com.yourorg.portfolio.dto.StockPriceDTO;
import com.yourorg.portfolio.model.Investment;
import com.yourorg.portfolio.model.Portfolio;
import com.yourorg.portfolio.model.Stock;
import com.yourorg.portfolio.model.User;
import com.yourorg.portfolio.repository.InvestmentRepository;
import com.yourorg.portfolio.repository.PortfolioRepository;
import com.yourorg.portfolio.repository.StockRepository;
import com.yourorg.portfolio.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PortfolioService {
    
    private final PortfolioRepository portfolioRepository;
    private final InvestmentRepository investmentRepository;
    private final StockRepository stockRepository;
    private final StockService stockService;
    private final UserService userService;
    
    @Autowired
    public PortfolioService(PortfolioRepository portfolioRepository,
                           InvestmentRepository investmentRepository,
                           StockRepository stockRepository,
                           StockService stockService,
                           UserService userService) {
        this.portfolioRepository = portfolioRepository;
        this.investmentRepository = investmentRepository;
        this.stockRepository = stockRepository;
        this.stockService = stockService;
        this.userService = userService;
    }
    
    /**
     * Get portfolio by user ID
     */
    public PortfolioDTO getPortfolioByUserId(Long userId) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "userId", userId));
        
        return buildPortfolioDTO(portfolio);
    }
    
    /**
     * Get or create portfolio for user
     */
    public Portfolio getOrCreatePortfolio(User user) {
        return portfolioRepository.findByUserId(user.getId())
            .orElseGet(() -> {
                Portfolio portfolio = new Portfolio();
                portfolio.setUser(user);
                portfolio.setName(user.getFirstName() + "'s Portfolio");
                return portfolioRepository.save(portfolio);
            });
    }
    
    /**
     * Buy stock (add investment)
     */
    public Investment buyStock(Long userId, String symbol, Integer quantity, BigDecimal buyPrice) {
        User user = userService.getUserEntityById(userId);
        Portfolio portfolio = getOrCreatePortfolio(user);
        
        // Get or create stock
        Stock stock = stockService.getOrCreateStock(symbol);
        
        // Check bank balance
        var bankAccount = userService.getOrCreateBankAccount(userId);
        BigDecimal totalCost = buyPrice.multiply(BigDecimal.valueOf(quantity));
        
        if (bankAccount.getCurrentBalance().compareTo(totalCost) < 0) {
            throw new IllegalArgumentException("Insufficient bank balance");
        }
        
        // Create investment
        Investment investment = new Investment();
        investment.setPortfolio(portfolio);
        investment.setStock(stock);
        investment.setQuantity(quantity);
        investment.setBuyPrice(buyPrice);
        investment.setBuyDate(LocalDate.now());
        investment.setAssetType("STOCK");
        
        Investment savedInvestment = investmentRepository.save(investment);
        
        // Update bank balance
        userService.updateBankBalance(userId, totalCost.negate());
        
        return savedInvestment;
    }
    
    /**
     * Sell stock (remove or reduce investment)
     */
    public void sellStock(Long userId, Long investmentId, Integer quantity) {
        Investment investment = investmentRepository.findById(investmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Investment", "id", investmentId));
        
        // Verify ownership
        if (!investment.getPortfolio().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Investment does not belong to user");
        }
        
        if (quantity > investment.getQuantity()) {
            throw new IllegalArgumentException("Cannot sell more than owned");
        }
        
        // Get current price
        StockPriceDTO currentPrice = stockService.getCurrentPrice(investment.getStock().getSymbol());
        
        // Calculate proceeds
        BigDecimal proceeds = currentPrice.getCurrentPrice().multiply(BigDecimal.valueOf(quantity));
        
        // Update or remove investment
        if (quantity.equals(investment.getQuantity())) {
            investmentRepository.delete(investment);
        } else {
            investment.setQuantity(investment.getQuantity() - quantity);
            investmentRepository.save(investment);
        }
        
        // Update bank balance
        userService.updateBankBalance(userId, proceeds);
    }
    
    /**
     * Build Portfolio DTO with current prices
     */
    private PortfolioDTO buildPortfolioDTO(Portfolio portfolio) {
        PortfolioDTO dto = new PortfolioDTO();
        dto.setPortfolioId(portfolio.getId());
        dto.setUserId(portfolio.getUser().getId());
        dto.setUserName(portfolio.getUser().getFirstName() + " " + portfolio.getUser().getLastName());
        dto.setPortfolioName(portfolio.getName());
        dto.setDescription(portfolio.getDescription());
        
        List<Investment> investments = investmentRepository.findByPortfolioId(portfolio.getId());
        List<PortfolioDTO.HoldingDTO> holdings = new ArrayList<>();
        
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        
        for (Investment investment : investments) {
            StockPriceDTO currentPrice = stockService.getCurrentPrice(investment.getStock().getSymbol());
            
            PortfolioDTO.HoldingDTO holding = new PortfolioDTO.HoldingDTO();
            holding.setInvestmentId(investment.getId());
            holding.setSymbol(investment.getStock().getSymbol());
            holding.setCompanyName(investment.getStock().getCompanyName());
            holding.setQuantity(investment.getQuantity());
            holding.setBuyPrice(investment.getBuyPrice());
            holding.setCurrentPrice(currentPrice.getCurrentPrice());
            holding.setBuyDate(investment.getBuyDate().toString());
            
            BigDecimal currentValue = currentPrice.getCurrentPrice()
                .multiply(BigDecimal.valueOf(investment.getQuantity()));
            holding.setCurrentValue(currentValue);
            
            BigDecimal profitLoss = currentValue.subtract(
                investment.getBuyPrice().multiply(BigDecimal.valueOf(investment.getQuantity()))
            );
            holding.setProfitLoss(profitLoss);
            
            BigDecimal profitLossPercent = profitLoss
                .divide(investment.getBuyPrice().multiply(BigDecimal.valueOf(investment.getQuantity())), 
                       4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            holding.setProfitLossPercentage(profitLossPercent);
            
            holdings.add(holding);
            
            totalValue = totalValue.add(currentValue);
            totalCost = totalCost.add(
                investment.getBuyPrice().multiply(BigDecimal.valueOf(investment.getQuantity()))
            );
        }
        
        dto.setHoldings(holdings);
        dto.setTotalValue(totalValue);
        dto.setTotalCost(totalCost);
        
        BigDecimal totalProfitLoss = totalValue.subtract(totalCost);
        dto.setTotalProfitLoss(totalProfitLoss);
        
        if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalProfitLossPercent = totalProfitLoss
                .divide(totalCost, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            dto.setTotalProfitLossPercentage(totalProfitLossPercent);
        } else {
            dto.setTotalProfitLossPercentage(BigDecimal.ZERO);
        }
        
        // Asset allocation (simplified - all stocks for now)
        PortfolioDTO.AssetAllocationDTO allocation = new PortfolioDTO.AssetAllocationDTO();
        allocation.setStocks(totalValue);
        allocation.setBonds(BigDecimal.ZERO);
        allocation.setCrypto(BigDecimal.ZERO);
        
        // Get cash from bank account
        var bankAccount = userService.getOrCreateBankAccount(portfolio.getUser().getId());
        allocation.setCash(bankAccount.getCurrentBalance());
        
        dto.setAssetAllocation(allocation);
        
        return dto;
    }
}

