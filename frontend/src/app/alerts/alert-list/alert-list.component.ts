import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AlertService } from '../../core/services/alert.service';
import { Alert, CreateAlertRequest } from '../../core/models/alert.model';

@Component({
  selector: 'app-alert-list',
  templateUrl: './alert-list.component.html',
})
export class AlertListComponent implements OnInit {
  alerts: Alert[] = [];
  loading = true;
  showForm = false;
  alertForm!: FormGroup;
  submitting = false;
  errorMessage = '';

  constructor(private alertService: AlertService, private fb: FormBuilder) {}

  ngOnInit(): void {
    this.alertForm = this.fb.group({
      symbol: ['', [Validators.required, Validators.pattern(/^[A-Za-z]{1,10}$/)]],
      alertType: ['PRICE_ABOVE', Validators.required],
      targetPrice: ['', [Validators.required, Validators.min(0.0001)]],
    });
    this.loadAlerts();
  }

  loadAlerts(): void {
    this.alertService.getAlerts().subscribe({
      next: (res) => {
        this.alerts = res.data;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  createAlert(): void {
    if (this.alertForm.invalid) return;
    this.submitting = true;
    this.errorMessage = '';

    const request: CreateAlertRequest = this.alertForm.value;
    this.alertService.createAlert(request).subscribe({
      next: (res) => {
        this.alerts.unshift(res.data);
        this.alertForm.reset({ alertType: 'PRICE_ABOVE' });
        this.showForm = false;
        this.submitting = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to create alert';
        this.submitting = false;
      },
    });
  }

  deleteAlert(id: number): void {
    if (!confirm('Delete this alert?')) return;
    this.alertService.deleteAlert(id).subscribe({
      next: () => (this.alerts = this.alerts.filter((a) => a.id !== id)),
      error: (err) => alert(err.error?.message || 'Failed to delete alert'),
    });
  }
}
