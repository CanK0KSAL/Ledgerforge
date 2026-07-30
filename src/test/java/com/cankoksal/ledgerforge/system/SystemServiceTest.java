package com.cankoksal.ledgerforge.system;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SystemServiceTest {

    @Test
    void createEcho_shouldNormalizeMessageAndCalculateLength() {
        SystemService systemService = new SystemService();

        EchoRequest request = new EchoRequest("  LedgerForge  ");

        EchoResponse response = systemService.createEcho(request);

        assertEquals("  LedgerForge  ", response.originalMessage());
        assertEquals("LedgerForge", response.normalizedMessage());
        assertEquals(11, response.length());
    }
}