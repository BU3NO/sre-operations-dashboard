package com.sre.dashboard.service;

import com.sre.dashboard.dto.DashboardSummaryResponse;
import com.sre.dashboard.dto.ServiceRequest;
import com.sre.dashboard.dto.ServiceResponse;
import com.sre.dashboard.dto.StatusUpdateRequest;
import com.sre.dashboard.entity.MonitoredService;
import com.sre.dashboard.entity.ServiceEnvironment;
import com.sre.dashboard.entity.ServiceStatus;
import com.sre.dashboard.exception.ResourceNotFoundException;
import com.sre.dashboard.repository.MonitoredServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitoredServiceServiceTest {

    @Mock
    private MonitoredServiceRepository repository;

    @InjectMocks
    private MonitoredServiceService service;

    private MonitoredService entity(Long id, String name, ServiceStatus status) {
        MonitoredService e = new MonitoredService();
        e.setId(id);
        e.setName(name);
        e.setEnvironment(ServiceEnvironment.PRODUCTION);
        e.setUrl("https://example.com/health");
        e.setStatus(status);
        return e;
    }

    @Test
    void createShouldDefaultStatusToUpWhenNotInformed() {
        ServiceRequest request = new ServiceRequest("Payments API", "desc", ServiceEnvironment.PRODUCTION,
                "https://payments.example.com/health", null);
        when(repository.save(any(MonitoredService.class))).thenAnswer(inv -> {
            MonitoredService e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });

        ServiceResponse response = service.create(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(ServiceStatus.UP);
        assertThat(response.name()).isEqualTo("Payments API");
    }

    @Test
    void updateStatusShouldChangeStatus() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity(1L, "Customer API", ServiceStatus.UP)));
        when(repository.save(any(MonitoredService.class))).thenAnswer(inv -> inv.getArgument(0));

        ServiceResponse response = service.updateStatus(1L, new StatusUpdateRequest(ServiceStatus.DOWN));

        assertThat(response.status()).isEqualTo(ServiceStatus.DOWN);
    }

    @Test
    void findByIdShouldThrowWhenServiceDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteShouldRemoveExistingService() {
        MonitoredService existing = entity(2L, "Billing Service", ServiceStatus.DEGRADED);
        when(repository.findById(2L)).thenReturn(Optional.of(existing));

        service.delete(2L);

        verify(repository).delete(existing);
    }

    @Test
    void summaryShouldAggregateCountsByStatus() {
        when(repository.count()).thenReturn(5L);
        when(repository.countByStatus(ServiceStatus.UP)).thenReturn(3L);
        when(repository.countByStatus(ServiceStatus.DEGRADED)).thenReturn(1L);
        when(repository.countByStatus(ServiceStatus.DOWN)).thenReturn(1L);

        DashboardSummaryResponse summary = service.summary();

        assertThat(summary.total()).isEqualTo(5);
        assertThat(summary.up()).isEqualTo(3);
        assertThat(summary.degraded()).isEqualTo(1);
        assertThat(summary.down()).isEqualTo(1);
    }
}
