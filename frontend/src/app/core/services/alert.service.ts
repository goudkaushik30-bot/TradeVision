import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Alert, CreateAlertRequest } from '../models/alert.model';
import { ApiResponse } from './auth.service';

@Injectable({ providedIn: 'root' })
export class AlertService {
  private readonly baseUrl = `${environment.alertServiceUrl}/api/alerts`;

  constructor(private http: HttpClient) {}

  createAlert(request: CreateAlertRequest): Observable<ApiResponse<Alert>> {
    return this.http.post<ApiResponse<Alert>>(this.baseUrl, request);
  }

  getAlerts(activeOnly = false): Observable<ApiResponse<Alert[]>> {
    const params = new HttpParams().set('activeOnly', String(activeOnly));
    return this.http.get<ApiResponse<Alert[]>>(this.baseUrl, { params });
  }

  deleteAlert(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }
}
