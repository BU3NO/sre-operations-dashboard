import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import {
  DashboardSummary,
  MonitoredService,
  ServiceRequest,
  ServiceStatus,
  SystemInfo
} from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  getServices(): Observable<MonitoredService[]> {
    return this.http.get<MonitoredService[]>(`${this.baseUrl}/services`);
  }

  getService(id: number): Observable<MonitoredService> {
    return this.http.get<MonitoredService>(`${this.baseUrl}/services/${id}`);
  }

  createService(request: ServiceRequest): Observable<MonitoredService> {
    return this.http.post<MonitoredService>(`${this.baseUrl}/services`, request);
  }

  updateService(id: number, request: ServiceRequest): Observable<MonitoredService> {
    return this.http.put<MonitoredService>(`${this.baseUrl}/services/${id}`, request);
  }

  updateStatus(id: number, status: ServiceStatus): Observable<MonitoredService> {
    return this.http.patch<MonitoredService>(`${this.baseUrl}/services/${id}/status`, { status });
  }

  deleteService(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/services/${id}`);
  }

  getSummary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.baseUrl}/dashboard/summary`);
  }

  getSystemInfo(): Observable<SystemInfo> {
    return this.http.get<SystemInfo>(`${this.baseUrl}/system/info`);
  }
}
