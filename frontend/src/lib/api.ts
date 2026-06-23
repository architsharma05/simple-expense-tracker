const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export type ApiResponse<T> = {
  success: boolean;
  data: T;
  error: string | null;
  timestamp: string;
};

export type User = { id: string; email: string; createdAt: string };
export type AuthResponse = { tokenType: string; accessToken: string; expiresInSeconds: number; user: User };
export type TransactionType = "INCOME" | "EXPENSE";
export type Transaction = {
  id: string;
  type: TransactionType;
  category: string;
  amount: number;
  description: string | null;
  transactionDate: string;
  createdAt: string;
};
export type Budget = { id: string; category: string; monthlyLimit: number; createdAt: string; updatedAt: string };
export type Goal = { id: string; goalName: string; targetAmount: number; targetDate: string; daysRemaining: number; monthlySavingsRequired: number; createdAt: string; updatedAt: string };
export type RecurringExpense = { category: string; description: string | null; typicalAmount: number; occurrenceCount: number; firstSeen: string; lastSeen: string };
export type Anomaly = { category: string; currentMonthSpend: number; trailingMonthlyAverage: number; difference: number; explanation: string };
export type MonthEndForecast = { month: string; spentSoFar: number; projectedSpend: number; projectedIncome: number; projectedNet: number; riskLevel: string };
export type CategoryForecast = { category: string; currentSpend: number; projectedSpend: number; trailingMonthlyAverage: number; riskLevel: string };
export type CategorySuggestion = { category: string; confidence: number; reason: string; generatedByAi: boolean };
export type Receipt = { id: string; fileUrl: string; uploadedAt: string };
export type ReceiptExtraction = { merchant: string; amount: number; transactionDate: string; category: string; status: string; notes: string };
export type MonthlySummary = { month: string; income: number; expenses: number; net: number };
export type CategorySummary = { category: string; total: number };
export type MonthlyTrend = { month: string; income: number; expenses: number; net: number };
export type AiAnswer = { answer: string; generatedByAi: boolean; generatedAt: string };
export type AiInsight = { id: string; insightText: string; generatedAt: string };

export async function apiFetch<T>(path: string, init?: RequestInit & { token?: string }): Promise<T> {
  const headers = new Headers(init?.headers);
  if (!(init?.body instanceof FormData)) headers.set("Content-Type", "application/json");
  if (init?.token) headers.set("Authorization", `Bearer ${init.token}`);

  const response = await fetch(`${API_BASE_URL}${path}`, { ...init, headers });
  const payload = (await response.json()) as ApiResponse<T>;

  if (!response.ok || !payload.success) {
    throw new Error(payload.error ?? `API request failed with status ${response.status}`);
  }

  return payload.data;
}
