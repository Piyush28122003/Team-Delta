package com.yourorg.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BankAccountRequestDTO {
    
    @NotBlank(message = "Account number is required")
    @Size(min = 8, max = 50, message = "Account number must be between 8 and 50 characters")
    private String accountNumber;
    
    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;
    
    private String accountType = "CHECKING";
    
    public BankAccountRequestDTO() {}
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}

