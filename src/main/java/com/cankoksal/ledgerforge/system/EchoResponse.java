package com.cankoksal.ledgerforge.system;

public record EchoResponse(
        String originalMessage,
        String normalizedMessage,
        int length
) {
}