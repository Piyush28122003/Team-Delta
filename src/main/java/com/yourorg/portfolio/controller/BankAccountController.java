package com.yourorg.portfolio.controller;

import com.yourorg.portfolio.dto.BankAccountDTO;
import com.yourorg.portfolio.dto.BankAccountRequestDTO;
import com.yourorg.portfolio.dto.TransactionRequestDTO;
import com.yourorg.portfolio.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bank-account")
@CrossOrigin(origins = "*")
public class BankAccountController {
    
    private final UserService userService;
    
    @Autowired
    public BankAccountController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<BankAccountDTO> getBankAccount(@PathVariable Long userId) {
        BankAccountDTO account = userService.getBankAccountDTO(userId);
        return ResponseEntity.ok(account);
    }
    
    @PostMapping("/user/{userId}/create")
    public ResponseEntity<BankAccountDTO> createBankAccount(
            @PathVariable Long userId,
            @Valid @RequestBody BankAccountRequestDTO request) {
        BankAccountDTO account = userService.createBankAccount(userId, request);
        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }
    
    @PutMapping("/user/{userId}/update")
    public ResponseEntity<BankAccountDTO> updateBankAccount(
            @PathVariable Long userId,
            @Valid @RequestBody BankAccountRequestDTO request) {
        BankAccountDTO account = userService.updateBankAccount(userId, request);
        return ResponseEntity.ok(account);
    }
    
    @PostMapping("/user/{userId}/deposit")
    public ResponseEntity<BankAccountDTO> deposit(
            @PathVariable Long userId,
            @Valid @RequestBody TransactionRequestDTO request) {
        BankAccountDTO account = userService.deposit(userId, request.getAmount(), request.getDescription());
        return ResponseEntity.ok(account);
    }
    
    @PostMapping("/user/{userId}/withdraw")
    public ResponseEntity<?> withdraw(
            @PathVariable Long userId,
            @Valid @RequestBody TransactionRequestDTO request) {
        try {
            BankAccountDTO account = userService.withdraw(userId, request.getAmount(), request.getDescription());
            return ResponseEntity.ok(account);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // Simple error response class
    private static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}

