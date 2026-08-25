package com.cankoksal.ledgerforge.auth;

import com.cankoksal.ledgerforge.security.JwtService;
import com.cankoksal.ledgerforge.user.User;
import com.cankoksal.ledgerforge.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

}