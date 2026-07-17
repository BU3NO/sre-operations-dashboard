import { Component, Input } from '@angular/core';
import { ServiceStatus } from '../models';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  template: `
    <span class="badge" [class]="'badge badge-' + status.toLowerCase()">
      <span class="dot"></span>{{ status }}
    </span>
  `
})
export class StatusBadgeComponent {
  @Input({ required: true }) status!: ServiceStatus;
}
