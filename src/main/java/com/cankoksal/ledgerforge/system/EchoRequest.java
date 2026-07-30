package com.cankoksal.ledgerforge.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EchoRequest(

        @NotBlank(message = "Message must not be blank")
        @Size(max = 100, message = "Message must not exceed 100 characters")
        String message

) {
}