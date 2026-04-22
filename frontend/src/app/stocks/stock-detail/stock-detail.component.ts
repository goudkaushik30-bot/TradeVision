import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { StockService } from '../../core/services/stock.service';
import { Stock } from '../../core/models/stock.model';

@Component({
  selector: 'app-stock-detail',
  templateUrl: './stock-detail.component.html',
})
export class StockDetailComponent implements OnInit {
  stock: Stock | null = null;
  loading = true;
  symbol = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private stockService: StockService
  ) {}

  ngOnInit(): void {
    this.symbol = this.route.snapshot.paramMap.get('symbol') || '';
    this.loadStock();
  }

  loadStock(): void {
    this.stockService.getStockBySymbol(this.symbol).subscribe({
      next: (res) => {
        this.stock = res.data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.router.navigate(['/stocks']);
      },
    });
  }

  addToWatchlist(): void {
    this.stockService.addToWatchlist(this.symbol).subscribe({
      next: () => alert(`${this.symbol} added to watchlist`),
      error: (err) => alert(err.error?.message || 'Failed to add to watchlist'),
    });
  }
}
