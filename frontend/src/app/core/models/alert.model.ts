export type AlertType = 'PRICE_ABOVE' | 'PRICE_BELOW';

export interface Alert {
  id: number;
  userId: number;
  symbol: string;
  alertType: AlertType;
  targetPrice: number;
  active: boolean;
  triggeredAt: string | null;
  createdAt: string;
}

export interface CreateAlertRequest {
  symbol: string;
  alertType: AlertType;
  targetPrice: number;
}
