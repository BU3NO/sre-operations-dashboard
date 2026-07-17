package com.sre.dashboard.service;

import com.sre.dashboard.dto.DashboardSummaryResponse;
import com.sre.dashboard.dto.ServiceRequest;
import com.sre.dashboard.dto.ServiceResponse;
import com.sre.dashboard.dto.StatusUpdateRequest;
import com.sre.dashboard.entity.MonitoredService;
import com.sre.dashboard.entity.ServiceStatus;
import com.sre.dashboard.exception.ResourceNotFoundException;
import com.sre.dashboard.repository.MonitoredServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MonitoredServiceService {

    private static final Logger log = LoggerFactory.getLogger(MonitoredServiceService.class);

    private final MonitoredServiceRepository repository;

    public MonitoredServiceService(MonitoredServiceRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> findAll() {
        return repository.findAll(Sort.by("id")).stream()
                .map(ServiceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceResponse findById(Long id) {
        return ServiceResponse.from(getOrThrow(id));
    }

    @Transactional
    public ServiceResponse create(ServiceRequest request) {
        MonitoredService entity = new MonitoredService();
        applyRequest(entity, request);
        if (entity.getStatus() == null) {
            entity.setStatus(ServiceStatus.UP);
        }
        MonitoredService saved = repository.save(entity);
        log.info("Serviço criado: id={} name={} environment={} status={}",
                saved.getId(), saved.getName(), saved.getEnvironment(), saved.getStatus());
        return ServiceResponse.from(saved);
    }

    @Transactional
    public ServiceResponse update(Long id, ServiceRequest request) {
        MonitoredService entity = getOrThrow(id);
        applyRequest(entity, request);
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        MonitoredService saved = repository.save(entity);
        log.info("Serviço atualizado: id={} name={} status={}", saved.getId(), saved.getName(), saved.getStatus());
        return ServiceResponse.from(saved);
    }

    @Transactional
    public ServiceResponse updateStatus(Long id, StatusUpdateRequest request) {
        MonitoredService entity = getOrThrow(id);
        ServiceStatus previous = entity.getStatus();
        entity.setStatus(request.status());
        MonitoredService saved = repository.save(entity);
        log.info("Status alterado: id={} name={} {} -> {}", saved.getId(), saved.getName(), previous, saved.getStatus());
        return ServiceResponse.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        MonitoredService entity = getOrThrow(id);
        repository.delete(entity);
        log.info("Serviço removido: id={} name={}", entity.getId(), entity.getName());
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary() {
        long total = repository.count();
        long up = repository.countByStatus(ServiceStatus.UP);
        long degraded = repository.countByStatus(ServiceStatus.DEGRADED);
        long down = repository.countByStatus(ServiceStatus.DOWN);
        return new DashboardSummaryResponse(total, up, degraded, down);
    }

    private MonitoredService getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado: id=" + id));
    }

    private void applyRequest(MonitoredService entity, ServiceRequest request) {
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setEnvironment(request.environment());
        entity.setUrl(request.url());
    }
}
