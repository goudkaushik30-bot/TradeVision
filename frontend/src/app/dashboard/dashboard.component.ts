import { Component, OnInit } from '@angular/core';
import { PortfolioService } from '../core/services/portfolio.service';
import { StockService } from '../core/services/stock.service';
import { AuthService } from '../core/services/auth.service';
import { PortfolioSummary } from '../core/models/portfolio.model';
import { Stock } from '../core/models/stock.model';
import { User } from '../core/models/user.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  summary: PortfolioSummary | null = null;
  topStocks: Stock[] = [];
  currentUser: User | null = null;
  loading = true;

  constructor(
    private portfolioService: PortfolioService,
    private stockService: StockService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.portfolioService.getPortfolioSummary().subscribe({
      next: (res) => {
        this.summary = res.data;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });

    this.stockService.getAllStocks().subscribe({
      next: (res) => (this.topStocks = res.data.slice(0, 5)),
    });
  }

  get pnlClass(): string {
    if (!this.summary) return '';
    return this.summary.totalProfitLoss >= 0 ? 'text-success' : 'text-danger';
  }
}
