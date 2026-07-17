export type ServiceEnvironment = 'DEVELOPMENT' | 'HOMOLOGATION' | 'PRODUCTION';

export type ServiceStatus = 'UP' | 'DEGRADED' | 'DOWN';

export interface MonitoredService {
  id: number;
  name: string;
  description: string | null;
  environment: ServiceEnvironment;
  url: string;
  status: ServiceStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ServiceRequest {
  name: string;
  description: string | null;
  environment: ServiceEnvironment;
  url: string;
  status: ServiceStatus;
}

export interface DashboardSummary {
  total: number;
  up: number;
  degraded: number;
  down: number;
}

export interface SystemInfo {
  application: string;
  version: string;
  environment: string;
  timestamp: string;
  javaVersion: string;
}
