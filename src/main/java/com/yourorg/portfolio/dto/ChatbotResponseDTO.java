package com.yourorg.portfolio.dto;

import java.util.List;

public class ChatbotResponseDTO {
    
    private String response;
    private boolean requiresConsent;
    private List<String> quickActions;
    private String timestamp;
    
    public ChatbotResponseDTO() {}
    
    public ChatbotResponseDTO(String response, boolean requiresConsent) {
        this.response = response;
        this.requiresConsent = requiresConsent;
    }
    
    public String getResponse() {
        return response;
    }
    
    public void setResponse(String response) {
        this.response = response;
    }
    
    public boolean isRequiresConsent() {
        return requiresConsent;
    }
    
    public void setRequiresConsent(boolean requiresConsent) {
        this.requiresConsent = requiresConsent;
    }
    
    public List<String> getQuickActions() {
        return quickActions;
    }
    
    public void setQuickActions(List<String> quickActions) {
        this.quickActions = quickActions;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

