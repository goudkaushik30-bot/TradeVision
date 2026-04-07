export interface Portfolio {
  id: number;
  userId: number;
  symbol: string;
  quantity: number;
  avgBuyPrice: number;
  currentValue: number;
  profitLoss: number;
  profitLossPercent: number;
}

export interface PortfolioSummary {
  userId: number;
  totalValue: number;
  totalCost: number;
  totalProfitLoss: number;
  totalProfitLossPercent: number;
  positionCount: number;
  positions: Portfolio[];
}
