package com.cankoksal.ledgerforge.auth;
import java.time.Instant;
import com.cankoksal.ledgerforge.user.User;


public record RegisterResponse(
    String email,
    String status,
    Instant createdAt
) {
public static RegisterResponse of(User user) {
    return new RegisterResponse(
            user.getEmail(),
            user.getStatus(),
            user.getCreatedAt()
    );
}
}