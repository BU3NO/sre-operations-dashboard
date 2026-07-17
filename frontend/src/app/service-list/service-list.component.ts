import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MonitoredService, ServiceStatus } from '../models';
import { StatusBadgeComponent } from '../status-badge/status-badge.component';

@Component({
  selector: 'app-service-list',
  standalone: true,
  imports: [DatePipe, StatusBadgeComponent],
  template: `
    @if (services.length === 0) {
      <div class="empty-state">
        <p>Nenhum serviço cadastrado ainda.</p>
        <p class="muted">Clique em "Novo serviço" para adicionar o primeiro.</p>
      </div>
    } @else {
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Serviço</th>
              <th>Ambiente</th>
              <th>URL</th>
              <th>Status</th>
              <th>Última atualização</th>
              <th class="actions-col">Ações</th>
            </tr>
          </thead>
          <tbody>
            @for (svc of services; track svc.id) {
              <tr>
                <td>
                  <div class="svc-name">{{ svc.name }}</div>
                  @if (svc.description) {
                    <div class="muted small">{{ svc.description }}</div>
                  }
                </td>
                <td><span class="env-tag">{{ svc.environment }}</span></td>
                <td class="url-cell">{{ svc.url }}</td>
                <td><app-status-badge [status]="svc.status" /></td>
                <td class="muted">{{ svc.updatedAt | date: 'dd/MM/yyyy HH:mm:ss' }}</td>
                <td class="actions">
                  <select
                    class="status-select"
                    [value]="svc.status"
                    (change)="onStatusChange(svc, $event)"
                    title="Alterar status">
                    <option value="UP">UP</option>
                    <option value="DEGRADED">DEGRADED</option>
                    <option value="DOWN">DOWN</option>
                  </select>
                  <button class="btn btn-secondary" (click)="edit.emit(svc)">Editar</button>
                  <button class="btn btn-danger" (click)="remove.emit(svc)">Excluir</button>
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `
})
export class ServiceListComponent {
  @Input({ required: true }) services: MonitoredService[] = [];
  @Output() edit = new EventEmitter<MonitoredService>();
  @Output() remove = new EventEmitter<MonitoredService>();
  @Output() statusChange = new EventEmitter<{ service: MonitoredService; status: ServiceStatus }>();

  onStatusChange(service: MonitoredService, event: Event): void {
    const status = (event.target as HTMLSelectElement).value as ServiceStatus;
    if (status !== service.status) {
      this.statusChange.emit({ service, status });
    }
  }
}
