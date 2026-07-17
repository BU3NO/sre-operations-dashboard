import { Component, EventEmitter, Input, OnChanges, Output, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MonitoredService, ServiceRequest } from '../models';

@Component({
  selector: 'app-service-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="modal-backdrop" (click)="cancelled.emit()">
      <div class="modal" (click)="$event.stopPropagation()">
        <h2>{{ service ? 'Editar serviço' : 'Novo serviço' }}</h2>
        <form [formGroup]="form" (ngSubmit)="submit()">
          <label>
            Nome *
            <input type="text" formControlName="name" maxlength="100" placeholder="Ex.: Customer API" />
            @if (form.controls.name.invalid && form.controls.name.touched) {
              <span class="field-error">Informe o nome do serviço.</span>
            }
          </label>

          <label>
            Descrição
            <textarea formControlName="description" maxlength="500" rows="2"
              placeholder="Descrição curta do serviço"></textarea>
          </label>

          <div class="form-row">
            <label>
              Ambiente *
              <select formControlName="environment">
                <option value="DEVELOPMENT">DEVELOPMENT</option>
                <option value="HOMOLOGATION">HOMOLOGATION</option>
                <option value="PRODUCTION">PRODUCTION</option>
              </select>
            </label>

            <label>
              Status *
              <select formControlName="status">
                <option value="UP">UP</option>
                <option value="DEGRADED">DEGRADED</option>
                <option value="DOWN">DOWN</option>
              </select>
            </label>
          </div>

          <label>
            URL *
            <input type="text" formControlName="url" maxlength="300"
              placeholder="https://servico.exemplo.com/health" />
            @if (form.controls.url.invalid && form.controls.url.touched) {
              <span class="field-error">Informe a URL do serviço.</span>
            }
          </label>

          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" (click)="cancelled.emit()">Cancelar</button>
            <button type="submit" class="btn btn-primary" [disabled]="form.invalid">
              {{ service ? 'Salvar alterações' : 'Cadastrar' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `
})
export class ServiceFormComponent implements OnChanges {
  @Input() service: MonitoredService | null = null;
  @Output() saved = new EventEmitter<ServiceRequest>();
  @Output() cancelled = new EventEmitter<void>();

  private readonly fb = inject(FormBuilder);

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: [''],
    environment: ['DEVELOPMENT', Validators.required],
    url: ['', [Validators.required, Validators.maxLength(300)]],
    status: ['UP', Validators.required]
  });

  ngOnChanges(): void {
    if (this.service) {
      this.form.patchValue({
        name: this.service.name,
        description: this.service.description ?? '',
        environment: this.service.environment,
        url: this.service.url,
        status: this.service.status
      });
    } else {
      this.form.reset();
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    this.saved.emit({
      name: value.name.trim(),
      description: value.description.trim() || null,
      environment: value.environment as ServiceRequest['environment'],
      url: value.url.trim(),
      status: value.status as ServiceRequest['status']
    });
  }
}
