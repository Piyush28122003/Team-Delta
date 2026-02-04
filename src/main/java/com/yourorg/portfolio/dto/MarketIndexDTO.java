package com.yourorg.portfolio.dto;

import java.math.BigDecimal;

public class MarketIndexDTO {

    private String name;
    private String symbol;
    private BigDecimal value;
    private BigDecimal change;
    private BigDecimal changePercent;
    private String trend; // "UP" or "DOWN"

    public MarketIndexDTO() {}

    public MarketIndexDTO(String name, String symbol, BigDecimal value, BigDecimal change, BigDecimal changePercent, String trend) {
        this.name = name;
        this.symbol = symbol;
        this.value = value;
        this.change = change;
        this.changePercent = changePercent;
        this.trend = trend;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public BigDecimal getChange() { return change; }
    public void setChange(BigDecimal change) { this.change = change; }

    public BigDecimal getChangePercent() { return changePercent; }
    public void setChangePercent(BigDecimal changePercent) { this.changePercent = changePercent; }

    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
}
