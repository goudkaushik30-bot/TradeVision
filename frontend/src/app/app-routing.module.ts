import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';

import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { StockListComponent } from './stocks/stock-list/stock-list.component';
import { StockDetailComponent } from './stocks/stock-detail/stock-detail.component';
import { TradeListComponent } from './trades/trade-list/trade-list.component';
import { ExecuteTradeComponent } from './trades/execute-trade/execute-trade.component';
import { PortfolioComponent } from './portfolio/portfolio.component';
import { AlertListComponent } from './alerts/alert-list/alert-list.component';

const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'auth/login', component: LoginComponent },
  { path: 'auth/register', component: RegisterComponent },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'stocks',
    component: StockListComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'stocks/:symbol',
    component: StockDetailComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'trades',
    component: TradeListComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'trades/execute',
    component: ExecuteTradeComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'portfolio',
    component: PortfolioComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'alerts',
    component: AlertListComponent,
    canActivate: [AuthGuard],
  },
  { path: '**', redirectTo: 'dashboard' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
