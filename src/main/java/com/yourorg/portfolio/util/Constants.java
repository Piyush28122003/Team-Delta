package com.yourorg.portfolio.util;

public class Constants {
    
    // Risk Categories
    public static final String RISK_CONSERVATIVE = "CONSERVATIVE";
    public static final String RISK_MODERATE = "MODERATE";
    public static final String RISK_AGGRESSIVE = "AGGRESSIVE";
    
    // Asset Types
    public static final String ASSET_TYPE_STOCK = "STOCK";
    public static final String ASSET_TYPE_BOND = "BOND";
    public static final String ASSET_TYPE_CRYPTO = "CRYPTO";
    public static final String ASSET_TYPE_CASH = "CASH";
    
    // Stock API Providers
    public static final String STOCK_API_ALPHA_VANTAGE = "alpha-vantage";
    public static final String STOCK_API_FINNHUB = "finnhub";
    public static final String STOCK_API_YAHOO_FINANCE = "yahoo-finance";
    
    // Chatbot Messages
    public static final String CHATBOT_CONSENT_MESSAGE = 
        "🔒 Privacy Notice: Do you allow me to access your portfolio and bank details to provide personalized investment advice?";
    
    public static final String CHATBOT_WELCOME_MESSAGE = 
        "👋 Hello! I'm your AI Portfolio Assistant. I can help you with:\n" +
        "• Portfolio value and performance\n" +
        "• Risk analysis\n" +
        "• Stock recommendations\n" +
        "• Investment insights\n\n" +
        "Type your question or use the quick actions below!";
    
    // Error Messages
    public static final String ERROR_USER_NOT_FOUND = "User not found";
    public static final String ERROR_PORTFOLIO_NOT_FOUND = "Portfolio not found";
    public static final String ERROR_STOCK_NOT_FOUND = "Stock not found";
    public static final String ERROR_INSUFFICIENT_BALANCE = "Insufficient bank balance";
    public static final String ERROR_INVALID_QUANTITY = "Invalid quantity";
    
    // Success Messages
    public static final String SUCCESS_STOCK_PURCHASED = "Stock purchased successfully";
    public static final String SUCCESS_STOCK_SOLD = "Stock sold successfully";
    
    private Constants() {
        // Utility class - prevent instantiation
    }
}

