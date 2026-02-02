package com.yourorg.portfolio.dto;

import java.math.BigDecimal;

public class UserDTO {
    
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private BankAccountDTO bankAccount;
    
    public UserDTO() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public BankAccountDTO getBankAccount() {
        return bankAccount;
    }
    
    public void setBankAccount(BankAccountDTO bankAccount) {
        this.bankAccount = bankAccount;
    }
    
    public static class BankAccountDTO {
        private Long id;
        private String accountNumber;
        private String bankName;
        private BigDecimal currentBalance;
        private String accountType;
        
        // Getters and Setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
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
        
        public BigDecimal getCurrentBalance() {
            return currentBalance;
        }
        
        public void setCurrentBalance(BigDecimal currentBalance) {
            this.currentBalance = currentBalance;
        }
        
        public String getAccountType() {
            return accountType;
        }
        
        public void setAccountType(String accountType) {
            this.accountType = accountType;
        }
    }
}

