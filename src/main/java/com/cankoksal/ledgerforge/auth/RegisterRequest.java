package com.cankoksal.ledgerforge.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be valid")
        @Size(max = 320, message = "Email must not exceed 320 characters")
        String email,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
        String password

) {
}