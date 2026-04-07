import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Portfolio, PortfolioSummary } from '../models/portfolio.model';
import { ApiResponse } from './auth.service';

@Injectable({ providedIn: 'root' })
export class PortfolioService {
  private readonly baseUrl = `${environment.portfolioServiceUrl}/api/portfolio`;

  constructor(private http: HttpClient) {}

  getPortfolio(): Observable<ApiResponse<Portfolio[]>> {
    return this.http.get<ApiResponse<Portfolio[]>>(this.baseUrl);
  }

  getPortfolioSummary(): Observable<ApiResponse<PortfolioSummary>> {
    return this.http.get<ApiResponse<PortfolioSummary>>(`${this.baseUrl}/summary`);
  }

  getPositionBySymbol(symbol: string): Observable<ApiResponse<Portfolio>> {
    return this.http.get<ApiResponse<Portfolio>>(`${this.baseUrl}/${symbol}`);
  }
}
