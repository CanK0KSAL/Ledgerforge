package com.cankoksal.ledgerforge.system;

import org.springframework.stereotype.Service;

@Service
public class SystemService {

    public EchoResponse createEcho(EchoRequest request) {
        String originalMessage = request.message();
        String normalizedMessage = normalize(originalMessage);

        return new EchoResponse(
                originalMessage,
                normalizedMessage,
                normalizedMessage.length()
        );
    }

    private String normalize(String message) {
        return message.trim();
    }
}