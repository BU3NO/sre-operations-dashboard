import { Component, OnInit, inject } from '@angular/core';
import { forkJoin } from 'rxjs';
import { ApiService } from '../api.service';
import {
  DashboardSummary,
  MonitoredService,
  ServiceRequest,
  ServiceStatus,
  SystemInfo
} from '../models';
import { ServiceFormComponent } from '../service-form/service-form.component';
import { ServiceListComponent } from '../service-list/service-list.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [ServiceListComponent, ServiceFormComponent],
  template: `
    <header class="app-header">
      <div>
        <h1>SRE Operations Dashboard</h1>
        <p class="muted">Gerenciamento de serviços monitorados</p>
      </div>
      @if (systemInfo) {
        <div class="env-info">
          <span class="env-tag env-tag-lg">{{ systemInfo.environment }}</span>
          <span class="muted small">v{{ systemInfo.version }} · Java {{ systemInfo.javaVersion }}</span>
        </div>
      }
    </header>

    <main>
      @if (error) {
        <div class="alert alert-error">
          <strong>Não foi possível carregar os dados.</strong>
          <span>{{ error }}</span>
          <button class="btn btn-secondary" (click)="load()">Tentar novamente</button>
        </div>
      }

      @if (loading) {
        <div class="loading">
          <div class="spinner"></div>
          <p class="muted">Carregando serviços...</p>
        </div>
      } @else if (!error) {
        @if (summary) {
          <section class="cards">
            <div class="card card-total">
              <span class="card-value">{{ summary.total }}</span>
              <span class="card-label">Total</span>
            </div>
            <div class="card card-up">
              <span class="card-value">{{ summary.up }}</span>
              <span class="card-label">UP</span>
            </div>
            <div class="card card-degraded">
              <span class="card-value">{{ summary.degraded }}</span>
              <span class="card-label">DEGRADED</span>
            </div>
            <div class="card card-down">
              <span class="card-value">{{ summary.down }}</span>
              <span class="card-label">DOWN</span>
            </div>
          </section>
        }

        <section class="toolbar">
          <h2>Serviços</h2>
          <div class="toolbar-actions">
            <button class="btn btn-secondary" (click)="load()">Atualizar</button>
            <button class="btn btn-primary" (click)="openCreateForm()">+ Novo serviço</button>
          </div>
        </section>

        @if (actionError) {
          <div class="alert alert-error">
            <span>{{ actionError }}</span>
          </div>
        }

        <app-service-list
          [services]="services"
          (edit)="openEditForm($event)"
          (remove)="deleteService($event)"
          (statusChange)="changeStatus($event)" />
      }
    </main>

    @if (showForm) {
      <app-service-form
        [service]="editing"
        (saved)="saveService($event)"
        (cancelled)="closeForm()" />
    }
  `
})
export class DashboardComponent implements OnInit {
  private readonly api = inject(ApiService);

  services: MonitoredService[] = [];
  summary: DashboardSummary | null = null;
  systemInfo: SystemInfo | null = null;
  loading = true;
  error: string | null = null;
  actionError: string | null = null;
  showForm = false;
  editing: MonitoredService | null = null;

  ngOnInit(): void {
    this.load();
    this.api.getSystemInfo().subscribe({
      next: (info) => (this.systemInfo = info),
      error: () => (this.systemInfo = null)
    });
  }

  load(): void {
    this.loading = true;
    this.error = null;
    this.actionError = null;
    forkJoin({
      services: this.api.getServices(),
      summary: this.api.getSummary()
    }).subscribe({
      next: ({ services, summary }) => {
        this.services = services;
        this.summary = summary;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.error = 'Verifique se o backend está no ar e tente novamente.';
      }
    });
  }

  openCreateForm(): void {
    this.editing = null;
    this.showForm = true;
  }

  openEditForm(service: MonitoredService): void {
    this.editing = service;
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
    this.editing = null;
  }

  saveService(request: ServiceRequest): void {
    const call = this.editing
      ? this.api.updateService(this.editing.id, request)
      : this.api.createService(request);

    call.subscribe({
      next: () => {
        this.closeForm();
        this.load();
      },
      error: () => (this.actionError = 'Não foi possível salvar o serviço. Verifique os dados e tente novamente.')
    });
  }

  deleteService(service: MonitoredService): void {
    if (!confirm(`Excluir o serviço "${service.name}"?`)) {
      return;
    }
    this.api.deleteService(service.id).subscribe({
      next: () => this.load(),
      error: () => (this.actionError = 'Não foi possível excluir o serviço.')
    });
  }

  changeStatus(event: { service: MonitoredService; status: ServiceStatus }): void {
    this.api.updateStatus(event.service.id, event.status).subscribe({
      next: () => this.load(),
      error: () => (this.actionError = 'Não foi possível alterar o status.')
    });
  }
}
