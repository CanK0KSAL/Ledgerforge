package com.cankoksal.ledgerforge.system;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final SystemService systemService;

    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of(
                "status", "ok",
                "service", "ledgerforge"
        );
    }

    @PostMapping("/echo")
    public EchoResponse echo(
            @Valid @RequestBody EchoRequest request
    ) {
        return systemService.createEcho(request);
    }
}