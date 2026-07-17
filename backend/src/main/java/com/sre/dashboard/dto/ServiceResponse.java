package com.sre.dashboard.dto;

import com.sre.dashboard.entity.MonitoredService;
import com.sre.dashboard.entity.ServiceEnvironment;
import com.sre.dashboard.entity.ServiceStatus;

import java.time.Instant;

public record ServiceResponse(
        Long id,
        String name,
        String description,
        ServiceEnvironment environment,
        String url,
        ServiceStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static ServiceResponse from(MonitoredService entity) {
        return new ServiceResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getEnvironment(),
                entity.getUrl(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
