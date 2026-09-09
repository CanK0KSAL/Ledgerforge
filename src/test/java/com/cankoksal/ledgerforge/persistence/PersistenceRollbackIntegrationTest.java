package com.cankoksal.ledgerforge.persistence;

import com.cankoksal.ledgerforge.user.User;
import com.cankoksal.ledgerforge.user.UserRepository;
import com.cankoksal.ledgerforge.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(PersistenceRollbackIntegrationTest.RollbackProbeService.class)
class PersistenceRollbackIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RollbackProbeService rollbackProbeService;

    @Test
    void shouldRollbackEvenAfterFlush() {

        UUID userId = UUID.randomUUID();

        // Transactional method içinde:
        // save -> flush -> RuntimeException
        //
        // RuntimeException bekliyoruz.
        assertThrows(
                RuntimeException.class,
                () -> rollbackProbeService.saveFlushThenFail(userId)
        );

        // flush sırasında INSERT SQL'i PostgreSQL'e gitmiş olsa bile
        // transaction rollback olduğu için kayıt kalıcı olmamalı.
        assertFalse(userRepository.existsById(userId));
    }


    static class RollbackProbeService {

        private final UserRepository userRepository;

        RollbackProbeService(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        @Transactional
        public void saveFlushThenFail(UUID userId) {

            Instant now = Instant.now();

            User user = new User(
                    userId,
                    "rollback-" + userId + "@ledgerforge.local",
                    "fake-hash",
                    "ACTIVE",
                    now,
                    now,
                    UserRole.USER
            );

            userRepository.save(user);

            // Persistence Context ile DB'yi şimdi senkronize etmeye zorla.
            // INSERT SQL'i PostgreSQL'e gidebilir.
            //
            // Ama flush COMMIT değildir.
            userRepository.flush();

            // Spring'in varsayılan transaction davranışında
            // RuntimeException transaction'ı rollback'e götürür.
            throw new RuntimeException("Forced rollback test");
        }
    }
}