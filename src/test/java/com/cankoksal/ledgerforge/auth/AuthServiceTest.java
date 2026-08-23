package com.cankoksal.ledgerforge.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.cankoksal.ledgerforge.user.UserRepository;
import com.cankoksal.ledgerforge.user.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private AuthService authService;

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
    void shouldRejectDuplicateEmail() {
        String email = "test@example.com";
        String rawPassword = "StrongPass123";
    
        when(userRepository.existsByEmail(email))
                .thenReturn(true);
    
        assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.register(email, rawPassword)
        );
    
        verify(userRepository, times(1))
                .existsByEmail(email);
    
        verify(userRepository, never())
                .save(any(User.class));
    
        verify(passwordEncoder, never())
                .encode(rawPassword);
    }
}