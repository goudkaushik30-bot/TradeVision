import { Component, OnInit } from '@angular/core';
import { StockService } from '../../core/services/stock.service';
import { Stock } from '../../core/models/stock.model';
import { debounceTime, distinctUntilChanged, Subject, switchMap } from 'rxjs';

@Component({
  selector: 'app-stock-list',
  templateUrl: './stock-list.component.html',
})
export class StockListComponent implements OnInit {
  stocks: Stock[] = [];
  filteredStocks: Stock[] = [];
  loading = true;
  searchQuery = '';
  private searchSubject = new Subject<string>();

  constructor(private stockService: StockService) {}

  ngOnInit(): void {
    this.loadStocks();
    this.setupSearch();
  }

  loadStocks(): void {
    this.stockService.getAllStocks().subscribe({
      next: (res) => {
        this.stocks = res.data;
        this.filteredStocks = res.data;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  setupSearch(): void {
    this.searchSubject
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((query) =>
          query.length >= 2
            ? this.stockService.searchStocks(query)
            : this.stockService.getAllStocks()
        )
      )
      .subscribe((res) => (this.filteredStocks = res.data));
  }

  onSearch(query: string): void {
    this.searchQuery = query;
    this.searchSubject.next(query);
  }

  addToWatchlist(symbol: string, event: Event): void {
    event.stopPropagation();
    this.stockService.addToWatchlist(symbol).subscribe({
      next: () => alert(`${symbol} added to watchlist`),
      error: (err) => alert(err.error?.message || 'Failed to add to watchlist'),
    });
  }
}
