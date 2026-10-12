package com.cankoksal.ledgerforge.security;

import com.cankoksal.ledgerforge.user.User;
import com.cankoksal.ledgerforge.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Value("${app.jwt.secret}")
    private String jwtSecret;


    // Test için USER veya ADMIN rolüne sahip sahte bir User oluşturuyoruz.
    // Bu user DB'ye kaydedilmiyor.
    // Amacımız JwtService ile gerçek JWT üretmek.
    private User createUser(UserRole role) {

        Instant now = Instant.now();

        return new User(
                UUID.randomUUID(),
                "security-test@ledgerforge.local",
                "fake-hash",
                "ACTIVE",
                now,
                now,
                role
        );
    }


    // Verilen role göre gerçek JwtService ile token üretir.
    private String createToken(UserRole role) {

        User user = createUser(role);

        return jwtService.generateToken(user);
    }


    @Test
    void protectedEndpointWithoutTokenShouldReturn401() throws Exception {

        // Protected endpoint'e JWT olmadan gidiyoruz.
        // Authentication oluşamayacağı için 401 bekliyoruz.

        mockMvc.perform(
                        get("/actuator/health")
                )
                .andExpect(status().isUnauthorized());
    }


    @Test
    void userTokenShouldAccessAuthenticatedEndpoint() throws Exception {

        String token = createToken(UserRole.USER);

        // USER authenticated bir kullanıcıdır.
        // /actuator/health sadece authenticated() istediği için erişebilir.

        mockMvc.perform(
                        get("/actuator/health")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }


    @Test
    void userTokenShouldNotAccessAdminEndpoint() throws Exception {

        String token = createToken(UserRole.USER);

        // JWT geçerli → Authentication oluşur.
        // Ama authority ROLE_USER.
        // /api/admin/** ise ROLE_ADMIN ister.
        // Kimlik var ama yetki yok → 403.

        mockMvc.perform(
                        get("/api/admin/test")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void adminTokenShouldAccessAdminEndpoint() throws Exception {

        String token = createToken(UserRole.ADMIN);

        // JWT geçerli ve authority ROLE_ADMIN.
        // Admin endpoint'e erişebilmesi gerekir.

        mockMvc.perform(
                        get("/api/admin/test")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(content().string("admin access granted"));
    }


    @Test
    void tamperedTokenShouldReturn401() throws Exception {

        String token = createToken(UserRole.USER);

        // JWT = header.payload.signature
        // Signature bölümünün ilk karakterini değiştiriyoruz.
        // Böylece signature gerçekten bozulmuş oluyor ve
        // JwtService token'ı doğrulamamalı.

        String[] parts = token.split("\\.");

        String signature = parts[2];

        char changedFirstCharacter =
                signature.charAt(0) == 'A' ? 'B' : 'A';

        String tamperedSignature =
                changedFirstCharacter + signature.substring(1);

        String tamperedToken =
                parts[0] + "." + parts[1] + "." + tamperedSignature;

        mockMvc.perform(
                        get("/actuator/health")
                                .header(
                                        "Authorization",
                                        "Bearer " + tamperedToken
                                )
                )
                .andExpect(status().isUnauthorized());
    }


    @Test
    void expiredTokenShouldReturn401() throws Exception {

        User user = createUser(UserRole.USER);

        // Aynı secret'ı kullanıyoruz ama token ömrünü negatif veriyoruz.
        // Böylece oluşturulan token daha doğrulanırken expired kabul edilmeli.
        JwtService expiredJwtService =
                new JwtService(jwtSecret, -1000);

        String expiredToken =
                expiredJwtService.generateToken(user);

        mockMvc.perform(
                        get("/actuator/health")
                                .header(
                                        "Authorization",
                                        "Bearer " + expiredToken
                                )
                )
                .andExpect(status().isUnauthorized());
    }
}