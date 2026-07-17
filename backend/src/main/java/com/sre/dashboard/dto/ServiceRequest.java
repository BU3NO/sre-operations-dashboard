package com.sre.dashboard.dto;

import com.sre.dashboard.entity.ServiceEnvironment;
import com.sre.dashboard.entity.ServiceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServiceRequest(
        @NotBlank(message = "name é obrigatório")
        @Size(max = 100, message = "name deve ter no máximo 100 caracteres")
        String name,

        @Size(max = 500, message = "description deve ter no máximo 500 caracteres")
        String description,

        @NotNull(message = "environment é obrigatório (DEVELOPMENT, HOMOLOGATION ou PRODUCTION)")
        ServiceEnvironment environment,

        @NotBlank(message = "url é obrigatória")
        @Size(max = 300, message = "url deve ter no máximo 300 caracteres")
        String url,

        ServiceStatus status
) {
}
