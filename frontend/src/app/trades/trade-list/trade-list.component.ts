import { Component, OnInit } from '@angular/core';
import { TradeService } from '../../core/services/trade.service';
import { Trade } from '../../core/models/trade.model';

@Component({
  selector: 'app-trade-list',
  templateUrl: './trade-list.component.html',
})
export class TradeListComponent implements OnInit {
  trades: Trade[] = [];
  loading = true;

  constructor(private tradeService: TradeService) {}

  ngOnInit(): void {
    this.tradeService.getTradeHistory().subscribe({
      next: (res) => {
        this.trades = res.data;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  cancelTrade(id: number): void {
    if (!confirm('Cancel this trade?')) return;
    this.tradeService.cancelTrade(id).subscribe({
      next: (res) => {
        const idx = this.trades.findIndex((t) => t.id === id);
        if (idx !== -1) this.trades[idx] = res.data;
      },
      error: (err) => alert(err.error?.message || 'Failed to cancel trade'),
    });
  }

  statusBadge(status: string): string {
    const map: Record<string, string> = {
      EXECUTED: 'bg-success',
      PENDING: 'bg-warning text-dark',
      CANCELLED: 'bg-secondary',
      FAILED: 'bg-danger',
    };
    return map[status] || 'bg-secondary';
  }
}
