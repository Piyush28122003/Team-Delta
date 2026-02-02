package com.yourorg.portfolio.service;

import com.yourorg.portfolio.dto.UserDTO;
import com.yourorg.portfolio.model.BankAccount;
import com.yourorg.portfolio.model.User;
import com.yourorg.portfolio.repository.BankAccountRepository;
import com.yourorg.portfolio.repository.UserRepository;
import com.yourorg.portfolio.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserService(UserRepository userRepository, 
                      BankAccountRepository bankAccountRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Create a new user
     */
    public UserDTO createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }
    
    /**
     * Get user by ID
     */
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return convertToDTO(user);
    }
    
    /**
     * Get user entity by ID (for internal use)
     */
    public User getUserEntityById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
    
    /**
     * Get all users
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Update user
     */
    public UserDTO updateUser(Long userId, User userDetails) {
        User user = getUserEntityById(userId);
        
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setPhone(userDetails.getPhone());
        
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }
    
    /**
     * Delete user
     */
    public void deleteUser(Long userId) {
        User user = getUserEntityById(userId);
        userRepository.delete(user);
    }
    
    /**
     * Get or create bank account for user
     */
    public BankAccount getOrCreateBankAccount(Long userId) {
        return bankAccountRepository.findByUserId(userId)
            .orElseGet(() -> {
                User user = getUserEntityById(userId);
                BankAccount account = new BankAccount();
                account.setUser(user);
                account.setAccountNumber("ACC" + System.currentTimeMillis());
                account.setBankName("HSBC Bank");
                account.setCurrentBalance(java.math.BigDecimal.ZERO);
                return bankAccountRepository.save(account);
            });
    }
    
    /**
     * Update bank account balance
     */
    public BankAccount updateBankBalance(Long userId, java.math.BigDecimal amount) {
        BankAccount account = getOrCreateBankAccount(userId);
        account.setCurrentBalance(account.getCurrentBalance().add(amount));
        return bankAccountRepository.save(account);
    }
    
    /**
     * Convert User entity to DTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        
        // Add bank account if exists
        bankAccountRepository.findByUserId(user.getId()).ifPresent(account -> {
            UserDTO.BankAccountDTO accountDTO = new UserDTO.BankAccountDTO();
            accountDTO.setId(account.getId());
            accountDTO.setAccountNumber(account.getAccountNumber());
            accountDTO.setBankName(account.getBankName());
            accountDTO.setCurrentBalance(account.getCurrentBalance());
            accountDTO.setAccountType(account.getAccountType());
            dto.setBankAccount(accountDTO);
        });
        
        return dto;
    }
}

