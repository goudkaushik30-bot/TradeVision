import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TradeService } from '../../core/services/trade.service';
import { StockService } from '../../core/services/stock.service';
import { Stock } from '../../core/models/stock.model';

@Component({
  selector: 'app-execute-trade',
  templateUrl: './execute-trade.component.html',
})
export class ExecuteTradeComponent implements OnInit {
  tradeForm!: FormGroup;
  loading = false;
  submitting = false;
  stock: Stock | null = null;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private tradeService: TradeService,
    private stockService: StockService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const symbol = this.route.snapshot.queryParamMap.get('symbol') || '';
    this.tradeForm = this.fb.group({
      symbol: [symbol, [Validators.required, Validators.pattern(/^[A-Za-z]{1,10}$/)]],
      quantity: ['', [Validators.required, Validators.min(0.0001)]],
      price: ['', [Validators.required, Validators.min(0.0001)]],
      tradeType: ['BUY', Validators.required],
    });

    if (symbol) {
      this.loadStock(symbol);
    }
  }

  loadStock(symbol: string): void {
    this.loading = true;
    this.stockService.getStockBySymbol(symbol.toUpperCase()).subscribe({
      next: (res) => {
        this.stock = res.data;
        this.tradeForm.patchValue({ price: res.data.currentPrice });
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  onSymbolBlur(): void {
    const symbol = this.tradeForm.get('symbol')?.value;
    if (symbol) this.loadStock(symbol.toUpperCase());
  }

  get estimatedTotal(): number {
    const qty = this.tradeForm.get('quantity')?.value || 0;
    const price = this.tradeForm.get('price')?.value || 0;
    return qty * price;
  }

  onSubmit(): void {
    if (this.tradeForm.invalid) return;
    this.submitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.tradeService.executeTrade(this.tradeForm.value).subscribe({
      next: (res) => {
        this.successMessage = `Trade executed! ID: ${res.data.id}`;
        this.submitting = false;
        setTimeout(() => this.router.navigate(['/trades']), 2000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Trade execution failed';
        this.submitting = false;
      },
    });
  }
}
