package com.cankoksal.ledgerforge.auth;

import com.cankoksal.ledgerforge.user.User;
import com.cankoksal.ledgerforge.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordPersistenceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registrationShouldPersistEncodedPasswordInsteadOfRawPassword() throws Exception {

        String email =
                "password-evidence-" + System.nanoTime() + "@ledgerforge.local";

        String rawPassword = "ClosurePass123!";

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "%s",
                                          "password": "%s"
                                        }
                                        """.formatted(email, rawPassword))
                )
                .andExpect(status().isCreated());

        User persistedUser = userRepository.findByEmail(email)
                .orElseThrow();

        assertNotEquals(
                rawPassword,
                persistedUser.getPasswordHash()
        );

        assertTrue(
                passwordEncoder.matches(
                        rawPassword,
                        persistedUser.getPasswordHash()
                )
        );

        userRepository.deleteById(persistedUser.getId());
    }
}