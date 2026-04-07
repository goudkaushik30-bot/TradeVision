import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Stock } from '../models/stock.model';
import { ApiResponse } from './auth.service';

@Injectable({ providedIn: 'root' })
export class StockService {
  private readonly baseUrl = `${environment.stockServiceUrl}/api/stocks`;

  constructor(private http: HttpClient) {}

  getAllStocks(): Observable<ApiResponse<Stock[]>> {
    return this.http.get<ApiResponse<Stock[]>>(this.baseUrl);
  }

  getStockBySymbol(symbol: string): Observable<ApiResponse<Stock>> {
    return this.http.get<ApiResponse<Stock>>(`${this.baseUrl}/${symbol}`);
  }

  searchStocks(query: string): Observable<ApiResponse<Stock[]>> {
    const params = new HttpParams().set('q', query);
    return this.http.get<ApiResponse<Stock[]>>(`${this.baseUrl}/search`, { params });
  }

  getWatchlist(): Observable<ApiResponse<Stock[]>> {
    return this.http.get<ApiResponse<Stock[]>>(`${this.baseUrl}/watchlist`);
  }

  addToWatchlist(symbol: string): Observable<ApiResponse<void>> {
    const params = new HttpParams().set('symbol', symbol);
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/watchlist`, null, { params });
  }

  removeFromWatchlist(symbol: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/watchlist/${symbol}`);
  }
}
