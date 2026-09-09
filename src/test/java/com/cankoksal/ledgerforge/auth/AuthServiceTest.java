package com.cankoksal.ledgerforge.auth;

import com.cankoksal.ledgerforge.security.JwtService;
import com.cankoksal.ledgerforge.user.User;
import com.cankoksal.ledgerforge.user.UserRepository;
import com.cankoksal.ledgerforge.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtService jwtService;

    @Test
    void shouldRegisterUser() {
    
        String email = "test@example.com";
        String rawPassword = "StrongPass123";
        String encodedPassword = "encoded-password";
    
        when(userRepository.existsByEmail(email))
                .thenReturn(false);
    
        when(passwordEncoder.encode(rawPassword))
                .thenReturn(encodedPassword);
    
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    
        User result = authService.register(email, rawPassword);
    
        assertNotNull(result);
        assertNotNull(result.getId());
    
        assertEquals(email, result.getEmail());
        assertEquals(encodedPassword, result.getPasswordHash());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(UserRole.USER, result.getRole());
    
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    
        assertEquals(result.getCreatedAt(), result.getUpdatedAt());
    
        verify(userRepository, times(1))
                .existsByEmail(email);
    
        verify(passwordEncoder, times(1))
                .encode(rawPassword);
    
        verify(userRepository, times(1))
                .save(any(User.class));
    }
    @Test
    void shouldLoginSuccessfully() {

        // GIVEN
        String email = "test@example.com";
        String rawPassword = "StrongPass123";
        String storedHash = "encoded-password";

        User user = new User(
                UUID.randomUUID(),
                email,
                storedHash,
                "ACTIVE",
                Instant.now(),
                Instant.now(),
                UserRole.USER
        );

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(rawPassword, storedHash))
                .thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("test-token");

        // WHEN
        String result = authService.login(email, rawPassword);

        // THEN
        assertEquals("test-token", result);

        verify(userRepository, times(1))
                .findByEmail(email);

        verify(passwordEncoder, times(1))
                .matches(rawPassword, storedHash);

        verify(jwtService, times(1))
                .generateToken(user);
    }
    @Test
    void shouldThrowWhenEmailDoesNotExist() {

        // GIVEN
        String email = "missing@example.com";
        String rawPassword = "StrongPass123";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(email, rawPassword)
        );

        verify(userRepository, times(1))
                .findByEmail(email);

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(jwtService, never())
                .generateToken(any(User.class));
    }

    @Test
    void shouldThrowWhenPasswordIsWrong() {

        // GIVEN
        String email = "test@example.com";
        String rawPassword = "WrongPassword";
        String storedHash = "encoded-password";

        User user = new User(
                UUID.randomUUID(),
                email,
                storedHash,
                "ACTIVE",
                Instant.now(),
                Instant.now(),
                UserRole.USER
        );

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(rawPassword, storedHash))
                .thenReturn(false);

        // WHEN + THEN
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(email, rawPassword)
        );

        verify(userRepository, times(1))
                .findByEmail(email);

        verify(passwordEncoder, times(1))
                .matches(rawPassword, storedHash);

        verify(jwtService, never())
                .generateToken(any(User.class));
    }
}