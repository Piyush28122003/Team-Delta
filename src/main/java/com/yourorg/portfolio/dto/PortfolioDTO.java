package com.yourorg.portfolio.dto;

import java.math.BigDecimal;
import java.util.List;

public class PortfolioDTO {
    
    private Long portfolioId;
    private Long userId;
    private String userName;
    private String portfolioName;
    private String description;
    private BigDecimal totalValue;
    private BigDecimal totalCost;
    private BigDecimal totalProfitLoss;
    private BigDecimal totalProfitLossPercentage;
    private List<HoldingDTO> holdings;
    private AssetAllocationDTO assetAllocation;
    
    public PortfolioDTO() {}
    
    // Getters and Setters
    public Long getPortfolioId() {
        return portfolioId;
    }
    
    public void setPortfolioId(Long portfolioId) {
        this.portfolioId = portfolioId;
    }
    
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
    
    public String getPortfolioName() {
        return portfolioName;
    }
    
    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getTotalValue() {
        return totalValue;
    }
    
    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }
    
    public BigDecimal getTotalCost() {
        return totalCost;
    }
    
    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
    
    public BigDecimal getTotalProfitLoss() {
        return totalProfitLoss;
    }
    
    public void setTotalProfitLoss(BigDecimal totalProfitLoss) {
        this.totalProfitLoss = totalProfitLoss;
    }
    
    public BigDecimal getTotalProfitLossPercentage() {
        return totalProfitLossPercentage;
    }
    
    public void setTotalProfitLossPercentage(BigDecimal totalProfitLossPercentage) {
        this.totalProfitLossPercentage = totalProfitLossPercentage;
    }
    
    public List<HoldingDTO> getHoldings() {
        return holdings;
    }
    
    public void setHoldings(List<HoldingDTO> holdings) {
        this.holdings = holdings;
    }
    
    public AssetAllocationDTO getAssetAllocation() {
        return assetAllocation;
    }
    
    public void setAssetAllocation(AssetAllocationDTO assetAllocation) {
        this.assetAllocation = assetAllocation;
    }
    
    // Inner DTOs
    public static class HoldingDTO {
        private Long investmentId;
        private String symbol;
        private String companyName;
        private Integer quantity;
        private BigDecimal buyPrice;
        private BigDecimal currentPrice;
        private BigDecimal currentValue;
        private BigDecimal profitLoss;
        private BigDecimal profitLossPercentage;
        private String buyDate;
        
        // Getters and Setters
        public Long getInvestmentId() {
            return investmentId;
        }
        
        public void setInvestmentId(Long investmentId) {
            this.investmentId = investmentId;
        }
        
        public String getSymbol() {
            return symbol;
        }
        
        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }
        
        public String getCompanyName() {
            return companyName;
        }
        
        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
        
        public BigDecimal getBuyPrice() {
            return buyPrice;
        }
        
        public void setBuyPrice(BigDecimal buyPrice) {
            this.buyPrice = buyPrice;
        }
        
        public BigDecimal getCurrentPrice() {
            return currentPrice;
        }
        
        public void setCurrentPrice(BigDecimal currentPrice) {
            this.currentPrice = currentPrice;
        }
        
        public BigDecimal getCurrentValue() {
            return currentValue;
        }
        
        public void setCurrentValue(BigDecimal currentValue) {
            this.currentValue = currentValue;
        }
        
        public BigDecimal getProfitLoss() {
            return profitLoss;
        }
        
        public void setProfitLoss(BigDecimal profitLoss) {
            this.profitLoss = profitLoss;
        }
        
        public BigDecimal getProfitLossPercentage() {
            return profitLossPercentage;
        }
        
        public void setProfitLossPercentage(BigDecimal profitLossPercentage) {
            this.profitLossPercentage = profitLossPercentage;
        }
        
        public String getBuyDate() {
            return buyDate;
        }
        
        public void setBuyDate(String buyDate) {
            this.buyDate = buyDate;
        }
    }
    
    public static class AssetAllocationDTO {
        private BigDecimal stocks;
        private BigDecimal bonds;
        private BigDecimal crypto;
        private BigDecimal cash;
        
        // Getters and Setters
        public BigDecimal getStocks() {
            return stocks;
        }
        
        public void setStocks(BigDecimal stocks) {
            this.stocks = stocks;
        }
        
        public BigDecimal getBonds() {
            return bonds;
        }
        
        public void setBonds(BigDecimal bonds) {
            this.bonds = bonds;
        }
        
        public BigDecimal getCrypto() {
            return crypto;
        }
        
        public void setCrypto(BigDecimal crypto) {
            this.crypto = crypto;
        }
        
        public BigDecimal getCash() {
            return cash;
        }
        
        public void setCash(BigDecimal cash) {
            this.cash = cash;
        }
    }
}

