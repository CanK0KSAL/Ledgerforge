package com.cankoksal.ledgerforge.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.temporal.ChronoUnit;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldSaveAndFindUserById() {

        UUID id = UUID.randomUUID();
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);

        User user = new User(
                id,
                "repository-test@ledgerforge.local",
                "fake-hash-for-test",
                "ACTIVE",
                now,
                now
        );

        userRepository.saveAndFlush(user);

        entityManager.clear();

        User foundUser = userRepository.findById(id)
                .orElseThrow();

        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getEmail(), foundUser.getEmail());
        assertEquals(user.getPasswordHash(), foundUser.getPasswordHash());
        assertEquals(user.getStatus(), foundUser.getStatus());
        assertEquals(user.getCreatedAt(), foundUser.getCreatedAt());
        assertEquals(user.getUpdatedAt(), foundUser.getUpdatedAt());
    }
}