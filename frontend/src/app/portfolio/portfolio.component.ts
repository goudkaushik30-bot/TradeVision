import { Component, OnInit } from '@angular/core';
import { PortfolioService } from '../core/services/portfolio.service';
import { PortfolioSummary } from '../core/models/portfolio.model';

@Component({
  selector: 'app-portfolio',
  templateUrl: './portfolio.component.html',
})
export class PortfolioComponent implements OnInit {
  summary: PortfolioSummary | null = null;
  loading = true;

  constructor(private portfolioService: PortfolioService) {}

  ngOnInit(): void {
    this.portfolioService.getPortfolioSummary().subscribe({
      next: (res) => {
        this.summary = res.data;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  get pnlClass(): string {
    if (!this.summary) return '';
    return this.summary.totalProfitLoss >= 0 ? 'text-success' : 'text-danger';
  }
}
