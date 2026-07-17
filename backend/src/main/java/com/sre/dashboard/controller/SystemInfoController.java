package com.sre.dashboard.controller;

import com.sre.dashboard.dto.SystemInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/system")
public class SystemInfoController {

    private final String environment;
    private final String version;

    public SystemInfoController(
            @Value("${app.environment}") String environment,
            @Value("${app.version}") String version) {
        this.environment = environment;
        this.version = version;
    }

    @GetMapping("/info")
    public SystemInfoResponse info() {
        return new SystemInfoResponse(
                "SRE Operations Dashboard",
                version,
                environment,
                Instant.now(),
                System.getProperty("java.version")
        );
    }
}
