package com.cankoksal.ledgerforge.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldCreateUserThroughServiceAndPersistIt() {

        String email = "service-" + UUID.randomUUID() + "@ledgerforge.local";
        String passwordHash = "integration-test-hash";

        User createdUser = userService.createUser(email, passwordHash);

        entityManager.flush();
        entityManager.clear();

        User persistedUser = userRepository.findById(createdUser.getId())
                .orElseThrow();

        assertNotNull(persistedUser.getId());
        assertEquals(email, persistedUser.getEmail());
        assertEquals(passwordHash, persistedUser.getPasswordHash());
        assertEquals("ACTIVE", persistedUser.getStatus());
        assertNotNull(persistedUser.getCreatedAt());
        assertNotNull(persistedUser.getUpdatedAt());
    }
}