import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Trade, TradeRequest } from '../models/trade.model';
import { ApiResponse } from './auth.service';

@Injectable({ providedIn: 'root' })
export class TradeService {
  private readonly baseUrl = `${environment.tradeServiceUrl}/api/trades`;

  constructor(private http: HttpClient) {}

  executeTrade(request: TradeRequest): Observable<ApiResponse<Trade>> {
    return this.http.post<ApiResponse<Trade>>(this.baseUrl, request);
  }

  getTradeHistory(): Observable<ApiResponse<Trade[]>> {
    return this.http.get<ApiResponse<Trade[]>>(this.baseUrl);
  }

  getTradeById(id: number): Observable<ApiResponse<Trade>> {
    return this.http.get<ApiResponse<Trade>>(`${this.baseUrl}/${id}`);
  }

  cancelTrade(id: number): Observable<ApiResponse<Trade>> {
    return this.http.delete<ApiResponse<Trade>>(`${this.baseUrl}/${id}`);
  }
}
