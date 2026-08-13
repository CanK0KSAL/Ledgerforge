package com.cankoksal.ledgerforge.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateActiveUser() {

        String email = "test@example.com";
        String passwordHash = "passwordHash";

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser(email, passwordHash);

        assertNotNull(result);
        assertNotNull(result.getId());

        assertEquals(email, result.getEmail());
        assertEquals(passwordHash, result.getPasswordHash());
        assertEquals("ACTIVE", result.getStatus());

        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        assertEquals(result.getCreatedAt(), result.getUpdatedAt());

        verify(userRepository, times(1))
                .save(any(User.class));
    }
}