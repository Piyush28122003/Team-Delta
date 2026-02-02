package com.yourorg.portfolio.service;

import com.yourorg.portfolio.dto.BankAccountDTO;
import com.yourorg.portfolio.dto.BankAccountRequestDTO;
import com.yourorg.portfolio.dto.LoginRequestDTO;
import com.yourorg.portfolio.dto.LoginResponseDTO;
import com.yourorg.portfolio.dto.SignupRequestDTO;
import com.yourorg.portfolio.dto.UserDTO;
import com.yourorg.portfolio.model.BankAccount;
import com.yourorg.portfolio.model.User;
import com.yourorg.portfolio.repository.BankAccountRepository;
import com.yourorg.portfolio.repository.UserRepository;
import com.yourorg.portfolio.exception.ResourceNotFoundException;
import com.yourorg.portfolio.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Autowired
    public UserService(UserRepository userRepository, 
                      BankAccountRepository bankAccountRepository,
                      PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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
     * Sign up a new user
     */
    public LoginResponseDTO signup(SignupRequestDTO signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setPhone(signupRequest.getPhone());
        
        User savedUser = userRepository.save(user);
        
        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getUsername());
        
        return new LoginResponseDTO(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getFirstName(),
            savedUser.getLastName(),
            token,
            "Signup successful"
        );
    }
    
    /**
     * Login user
     */
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        // Try to find user by username or email
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsernameOrEmail());
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(loginRequest.getUsernameOrEmail());
        }
        
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid username/email or password");
        }
        
        User user = userOpt.get();
        
        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username/email or password");
        }
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        return new LoginResponseDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            token,
            "Login successful"
        );
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
    public BankAccount updateBankBalance(Long userId, BigDecimal amount) {
        BankAccount account = getOrCreateBankAccount(userId);
        account.setCurrentBalance(account.getCurrentBalance().add(amount));
        return bankAccountRepository.save(account);
    }
    
    /**
     * Get bank account DTO
     */
    public BankAccountDTO getBankAccountDTO(Long userId) {
        BankAccount account = getOrCreateBankAccount(userId);
        return convertBankAccountToDTO(account);
    }
    
    /**
     * Create bank account
     */
    public BankAccountDTO createBankAccount(Long userId, BankAccountRequestDTO request) {
        User user = getUserEntityById(userId);
        
        // Check if account already exists
        if (bankAccountRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("Bank account already exists for this user");
        }
        
        // Check if account number already exists
        if (bankAccountRepository.existsByAccountNumber(request.getAccountNumber())) {
            throw new IllegalArgumentException("Account number already exists");
        }
        
        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setAccountNumber(request.getAccountNumber());
        account.setBankName(request.getBankName());
        account.setAccountType(request.getAccountType());
        account.setCurrentBalance(BigDecimal.ZERO);
        
        BankAccount savedAccount = bankAccountRepository.save(account);
        return convertBankAccountToDTO(savedAccount);
    }
    
    /**
     * Update bank account details
     */
    public BankAccountDTO updateBankAccount(Long userId, BankAccountRequestDTO request) {
        BankAccount account = getOrCreateBankAccount(userId);
        
        // Check if account number is being changed and if it already exists
        if (!account.getAccountNumber().equals(request.getAccountNumber())) {
            if (bankAccountRepository.existsByAccountNumber(request.getAccountNumber())) {
                throw new IllegalArgumentException("Account number already exists");
            }
        }
        
        account.setAccountNumber(request.getAccountNumber());
        account.setBankName(request.getBankName());
        account.setAccountType(request.getAccountType());
        
        BankAccount updatedAccount = bankAccountRepository.save(account);
        return convertBankAccountToDTO(updatedAccount);
    }
    
    /**
     * Deposit money to bank account
     */
    public BankAccountDTO deposit(Long userId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than 0");
        }
        
        BankAccount account = getOrCreateBankAccount(userId);
        account.setCurrentBalance(account.getCurrentBalance().add(amount));
        BankAccount updatedAccount = bankAccountRepository.save(account);
        return convertBankAccountToDTO(updatedAccount);
    }
    
    /**
     * Withdraw money from bank account
     */
    public BankAccountDTO withdraw(Long userId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than 0");
        }
        
        BankAccount account = getOrCreateBankAccount(userId);
        
        if (account.getCurrentBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance. Available: " + account.getCurrentBalance());
        }
        
        account.setCurrentBalance(account.getCurrentBalance().subtract(amount));
        BankAccount updatedAccount = bankAccountRepository.save(account);
        return convertBankAccountToDTO(updatedAccount);
    }
    
    /**
     * Convert BankAccount entity to DTO
     */
    private BankAccountDTO convertBankAccountToDTO(BankAccount account) {
        BankAccountDTO dto = new BankAccountDTO();
        dto.setId(account.getId());
        dto.setUserId(account.getUser().getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBankName(account.getBankName());
        dto.setCurrentBalance(account.getCurrentBalance());
        dto.setAccountType(account.getAccountType());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        return dto;
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

