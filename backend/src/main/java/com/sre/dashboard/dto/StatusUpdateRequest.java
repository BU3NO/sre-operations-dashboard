package com.sre.dashboard.dto;

import com.sre.dashboard.entity.ServiceStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "status é obrigatório (UP, DEGRADED ou DOWN)")
        ServiceStatus status
) {
}
