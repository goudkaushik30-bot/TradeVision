export type TradeType = 'BUY' | 'SELL';
export type TradeStatus = 'PENDING' | 'EXECUTED' | 'CANCELLED' | 'FAILED';

export interface Trade {
  id: number;
  userId: number;
  symbol: string;
  quantity: number;
  price: number;
  tradeType: TradeType;
  status: TradeStatus;
  totalValue: number;
  executedAt: string;
}

export interface TradeRequest {
  symbol: string;
  quantity: number;
  price: number;
  tradeType: TradeType;
}
