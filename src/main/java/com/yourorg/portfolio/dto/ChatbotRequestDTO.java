package com.yourorg.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChatbotRequestDTO {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Message is required")
    private String message;
    
    private Boolean consentGiven = false;
    
    public ChatbotRequestDTO() {}
    
    public ChatbotRequestDTO(Long userId, String message, Boolean consentGiven) {
        this.userId = userId;
        this.message = message;
        this.consentGiven = consentGiven;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Boolean getConsentGiven() {
        return consentGiven;
    }
    
    public void setConsentGiven(Boolean consentGiven) {
        this.consentGiven = consentGiven;
    }
}

