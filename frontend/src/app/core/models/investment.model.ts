export type InvestmentStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'COMPLETED';

export interface Investment {
  id: string;
  startupId: string;
  investorId: string;
  amount: number;
  status: InvestmentStatus;
  createdAt?: string;
}

export interface CreateInvestmentRequest {
  startupId: string;
  amount: number;
}
