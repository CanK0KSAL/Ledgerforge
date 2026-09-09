package com.cankoksal.ledgerforge.auth;

import com.cankoksal.ledgerforge.security.JwtService;
import com.cankoksal.ledgerforge.user.User;
import com.cankoksal.ledgerforge.user.UserRepository;
import com.cankoksal.ledgerforge.user.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService    jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService= jwtService;
    }

    @Transactional
    public User register(String email, String rawPassword) {

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        UUID id = UUID.randomUUID();
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);

        User user = new User(
                id,
                email,
                encodedPassword,
                "ACTIVE",
                now,
                now,
                UserRole.USER
        );

        return userRepository.save(user);
    }
    public String login(String email, String rawPassword) {

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            throw new InvalidCredentialsException(
                    "Email or password is incorrect"
            );
        }

        User user = optionalUser.get();

        String storedHash = user.getPasswordHash();

        boolean passwordMatches =
                passwordEncoder.matches(rawPassword, storedHash);

        if (!passwordMatches) {
            throw new InvalidCredentialsException(
                    "Email or password is incorrect"
            );
        }

        return jwtService.generateToken(user);
    }
}