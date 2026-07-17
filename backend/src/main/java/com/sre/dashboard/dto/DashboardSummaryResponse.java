package com.sre.dashboard.dto;

public record DashboardSummaryResponse(
        long total,
        long up,
        long degraded,
        long down
) {
}
