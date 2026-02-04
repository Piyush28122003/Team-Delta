package com.yourorg.portfolio.service;

import com.yourorg.portfolio.dto.*;
import com.yourorg.portfolio.exception.ResourceNotFoundException;
import com.yourorg.portfolio.model.BankAccount;
import com.yourorg.portfolio.model.User;
import com.yourorg.portfolio.repository.BankAccountRepository;
import com.yourorg.portfolio.repository.UserRepository;
import com.yourorg.portfolio.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private User user;
    private BankAccount bankAccount;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("johndoe");
        user.setEmail("john@example.com");
        user.setPassword("encodedPassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhone("1234567890");

        bankAccount = new BankAccount();
        bankAccount.setId(1L);
        bankAccount.setUser(user);
        bankAccount.setAccountNumber("ACC123");
        bankAccount.setBankName("HSBC Bank");
        bankAccount.setCurrentBalance(BigDecimal.ZERO);
    }

    @Nested
    @DisplayName("createUser")
    class CreateUserTests {
        @Test
        void shouldCreateUserWhenUsernameAndEmailAreUnique() {
            when(userRepository.existsByUsername("johndoe")).thenReturn(false);
            when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);

            User newUser = new User();
            newUser.setUsername("johndoe");
            newUser.setEmail("john@example.com");
            newUser.setPassword("plainPassword");
            newUser.setFirstName("John");
            newUser.setLastName("Doe");

            UserDTO result = userService.createUser(newUser);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("johndoe");
            assertThat(result.getEmail()).isEqualTo("john@example.com");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            verify(userRepository).save(any(User.class));
        }

        @Test
        void shouldThrowWhenUsernameAlreadyExists() {
            when(userRepository.existsByUsername("johndoe")).thenReturn(true);

            User newUser = new User();
            newUser.setUsername("johndoe");
            newUser.setEmail("john@example.com");
            newUser.setPassword("plainPassword");

            assertThatThrownBy(() -> userService.createUser(newUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Username already exists");
            verify(userRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenEmailAlreadyExists() {
            when(userRepository.existsByUsername("johndoe")).thenReturn(false);
            when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

            User newUser = new User();
            newUser.setUsername("johndoe");
            newUser.setEmail("john@example.com");
            newUser.setPassword("plainPassword");

            assertThatThrownBy(() -> userService.createUser(newUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email already exists");
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("signup")
    class SignupTests {
        @Test
        void shouldSignupUserAndReturnLoginResponse() {
            SignupRequestDTO request = new SignupRequestDTO();
            request.setUsername("johndoe");
            request.setEmail("john@example.com");
            request.setPassword("password123");
            request.setFirstName("John");
            request.setLastName("Doe");

            when(userRepository.existsByUsername("johndoe")).thenReturn(false);
            when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtUtil.generateToken(1L, "johndoe")).thenReturn("jwt-token");

            LoginResponseDTO result = userService.signup(request);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("johndoe");
            assertThat(result.getToken()).isEqualTo("jwt-token");
            assertThat(result.getMessage()).isEqualTo("Signup successful");
            verify(userRepository).save(any(User.class));
        }

        @Test
        void shouldThrowWhenSignupWithExistingUsername() {
            SignupRequestDTO request = new SignupRequestDTO();
            request.setUsername("johndoe");
            request.setEmail("john@example.com");
            request.setPassword("password123");

            when(userRepository.existsByUsername("johndoe")).thenReturn(true);

            assertThatThrownBy(() -> userService.signup(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Username already exists");
        }
    }

    @Nested
    @DisplayName("login")
    class LoginTests {
        @Test
        void shouldLoginByUsernameAndReturnToken() {
            LoginRequestDTO request = new LoginRequestDTO("johndoe", "password123");
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
            when(jwtUtil.generateToken(1L, "johndoe")).thenReturn("jwt-token");

            LoginResponseDTO result = userService.login(request);

            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo("jwt-token");
            assertThat(result.getMessage()).isEqualTo("Login successful");
        }

        @Test
        void shouldLoginByEmailWhenUsernameNotFound() {
            LoginRequestDTO request = new LoginRequestDTO("john@example.com", "password123");
            when(userRepository.findByUsername("john@example.com")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
            when(jwtUtil.generateToken(1L, "johndoe")).thenReturn("jwt-token");

            LoginResponseDTO result = userService.login(request);

            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo("jwt-token");
        }

        @Test
        void shouldThrowWhenUserNotFound() {
            LoginRequestDTO request = new LoginRequestDTO("unknown", "password123");
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid username/email or password");
        }

        @Test
        void shouldThrowWhenPasswordDoesNotMatch() {
            LoginRequestDTO request = new LoginRequestDTO("johndoe", "wrongpassword");
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid username/email or password");
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserByIdTests {
        @Test
        void shouldReturnUserDTOWhenUserExists() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(bankAccountRepository.findByUserId(1L)).thenReturn(Optional.empty());

            UserDTO result = userService.getUserById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("johndoe");
            assertThat(result.getEmail()).isEqualTo("john@example.com");
        }

        @Test
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsersTests {
        @Test
        void shouldReturnListOfUsers() {
            when(userRepository.findAll()).thenReturn(List.of(user));
            when(bankAccountRepository.findByUserId(1L)).thenReturn(Optional.empty());

            List<UserDTO> result = userService.getAllUsers();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUsername()).isEqualTo("johndoe");
        }

        @Test
        void shouldReturnEmptyListWhenNoUsers() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<UserDTO> result = userService.getAllUsers();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deposit and withdraw")
    class BankAccountTests {
        @Test
        void shouldDepositAndUpdateBalance() {
            bankAccount.setCurrentBalance(BigDecimal.valueOf(100));
            when(bankAccountRepository.findByUserId(1L)).thenReturn(Optional.of(bankAccount));
            BankAccount updatedAccount = new BankAccount();
            updatedAccount.setCurrentBalance(BigDecimal.valueOf(200));
            updatedAccount.setUser(user);
            updatedAccount.setAccountNumber("ACC123");
            updatedAccount.setBankName("HSBC Bank");
            when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(updatedAccount);

            BankAccountDTO result = userService.deposit(1L, BigDecimal.valueOf(100), "Deposit");

            assertThat(result).isNotNull();
            assertThat(result.getCurrentBalance()).isEqualByComparingTo(BigDecimal.valueOf(200));
            verify(bankAccountRepository).save(any(BankAccount.class));
        }

        @Test
        void shouldThrowWhenDepositAmountIsZeroOrNegative() {
            assertThatThrownBy(() -> userService.deposit(1L, BigDecimal.ZERO, "Deposit"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Deposit amount must be greater than 0");

            assertThatThrownBy(() -> userService.deposit(1L, BigDecimal.valueOf(-10), "Deposit"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Deposit amount must be greater than 0");
        }

        @Test
        void shouldWithdrawAndUpdateBalance() {
            bankAccount.setCurrentBalance(BigDecimal.valueOf(200));
            when(bankAccountRepository.findByUserId(1L)).thenReturn(Optional.of(bankAccount));
            BankAccount updatedAccount = new BankAccount();
            updatedAccount.setCurrentBalance(BigDecimal.valueOf(100));
            updatedAccount.setUser(user);
            updatedAccount.setAccountNumber("ACC123");
            updatedAccount.setBankName("HSBC Bank");
            when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(updatedAccount);

            BankAccountDTO result = userService.withdraw(1L, BigDecimal.valueOf(100), "Withdrawal");

            assertThat(result).isNotNull();
            assertThat(result.getCurrentBalance()).isEqualByComparingTo(BigDecimal.valueOf(100));
        }

        @Test
        void shouldThrowWhenInsufficientBalanceForWithdrawal() {
            bankAccount.setCurrentBalance(BigDecimal.valueOf(50));
            when(bankAccountRepository.findByUserId(1L)).thenReturn(Optional.of(bankAccount));

            assertThatThrownBy(() -> userService.withdraw(1L, BigDecimal.valueOf(100), "Withdrawal"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Insufficient balance");
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUserTests {
        @Test
        void shouldDeleteUserWhenExists() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            userService.deleteUser(1L);

            verify(userRepository).delete(user);
        }

        @Test
        void shouldThrowWhenDeletingNonExistentUser() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteUser(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
