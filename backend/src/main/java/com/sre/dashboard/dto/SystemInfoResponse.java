package com.sre.dashboard.dto;

import java.time.Instant;

public record SystemInfoResponse(
        String application,
        String version,
        String environment,
        Instant timestamp,
        String javaVersion
) {
}
