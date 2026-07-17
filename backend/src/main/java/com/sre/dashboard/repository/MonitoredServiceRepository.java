package com.sre.dashboard.repository;

import com.sre.dashboard.entity.MonitoredService;
import com.sre.dashboard.entity.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonitoredServiceRepository extends JpaRepository<MonitoredService, Long> {

    long countByStatus(ServiceStatus status);
}
