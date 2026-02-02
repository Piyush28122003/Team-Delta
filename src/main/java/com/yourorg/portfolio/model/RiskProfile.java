package com.yourorg.portfolio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_profiles")
public class RiskProfile {
    
    public enum RiskCategory {
        CONSERVATIVE, MODERATE, AGGRESSIVE
    }
    
    public enum InvestmentHorizon {
        SHORT, MEDIUM, LONG
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @NotNull(message = "User is required")
    private User user;
    
    @Column(name = "risk_category", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Risk category is required")
    private RiskCategory riskCategory;
    
    @Column(name = "volatility_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Volatility score cannot be negative")
    private BigDecimal volatilityScore = BigDecimal.ZERO;
    
    @Column(name = "diversification_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Diversification score cannot be negative")
    private BigDecimal diversificationScore = BigDecimal.ZERO;
    
    @Column(name = "max_loss_tolerance", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Max loss tolerance cannot be negative")
    private BigDecimal maxLossTolerance = BigDecimal.ZERO;
    
    @Column(name = "investment_horizon", length = 20)
    @Enumerated(EnumType.STRING)
    private InvestmentHorizon investmentHorizon = InvestmentHorizon.MEDIUM;
    
    @Column(name = "last_analyzed_at")
    private LocalDateTime lastAnalyzedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastAnalyzedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public RiskProfile() {}
    
    public RiskProfile(User user, RiskCategory riskCategory) {
        this.user = user;
        this.riskCategory = riskCategory;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public RiskCategory getRiskCategory() {
        return riskCategory;
    }
    
    public void setRiskCategory(RiskCategory riskCategory) {
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
    
    public InvestmentHorizon getInvestmentHorizon() {
        return investmentHorizon;
    }
    
    public void setInvestmentHorizon(InvestmentHorizon investmentHorizon) {
        this.investmentHorizon = investmentHorizon;
    }
    
    public LocalDateTime getLastAnalyzedAt() {
        return lastAnalyzedAt;
    }
    
    public void setLastAnalyzedAt(LocalDateTime lastAnalyzedAt) {
        this.lastAnalyzedAt = lastAnalyzedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

