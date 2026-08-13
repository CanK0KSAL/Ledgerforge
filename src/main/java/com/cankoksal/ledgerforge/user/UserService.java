package com.cankoksal.ledgerforge.user;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
}

public User createUser(String email, String passwordHash) {

    UUID id = UUID.randomUUID();
    Instant now = Instant.now();

    User user = new User(
            id,
            email,
            passwordHash,
            "ACTIVE",
            now,
            now
    );

    return userRepository.save(user);
}
}