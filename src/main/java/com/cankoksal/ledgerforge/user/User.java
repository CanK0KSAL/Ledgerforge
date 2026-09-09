package com.cankoksal.ledgerforge.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

@Id
@Column(name = "id", nullable = false)
private UUID id;

@Column(name = "email", nullable = false, unique = true, length = 320)
private String email;

@Column(name = "password_hash", nullable = false, length = 255)
private String passwordHash;

@Column(name = "status", nullable = false, length = 20)
private String status;

@Column(name = "created_at", nullable = false)
private Instant createdAt;

@Column(name = "updated_at", nullable = false)
private Instant updatedAt;

@Enumerated(EnumType.STRING)
@Column(name = "role", nullable = false, length = 20)
private UserRole role;

protected User() {
}

public User(
    UUID id,
    String email,
    String passwordHash,
    String status,
    Instant createdAt,
    Instant updatedAt,
    UserRole role
) {
this.id = id;
this.email = email;
this.passwordHash = passwordHash;
this.status = status;
this.createdAt = createdAt;
this.updatedAt = updatedAt;
this.role = role;
}

public UUID getId() {
    return id;
}

public String getEmail() {
    return email;
}

public String getPasswordHash() {
    return passwordHash;
}

public String getStatus() {
    return status;
}

public Instant getCreatedAt() {
    return createdAt;
}

public Instant getUpdatedAt() {
    return updatedAt;
}
public UserRole getRole() {
    return role;
}

}