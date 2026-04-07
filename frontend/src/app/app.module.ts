import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';

import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { StockListComponent } from './stocks/stock-list/stock-list.component';
import { StockDetailComponent } from './stocks/stock-detail/stock-detail.component';
import { TradeListComponent } from './trades/trade-list/trade-list.component';
import { ExecuteTradeComponent } from './trades/execute-trade/execute-trade.component';
import { PortfolioComponent } from './portfolio/portfolio.component';
import { AlertListComponent } from './alerts/alert-list/alert-list.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    DashboardComponent,
    StockListComponent,
    StockDetailComponent,
    TradeListComponent,
    ExecuteTradeComponent,
    PortfolioComponent,
    AlertListComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    CommonModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    AppRoutingModule,
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
