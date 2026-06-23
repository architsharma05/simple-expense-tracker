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
export type MonthlySummary = { month: string; income: number; expenses: number; net: number };
export type CategorySummary = { category: string; total: number };
export type MonthlyTrend = { month: string; income: number; expenses: number; net: number };

export async function apiFetch<T>(path: string, init?: RequestInit & { token?: string }): Promise<T> {
  const headers = new Headers(init?.headers);
  headers.set("Content-Type", "application/json");
  if (init?.token) headers.set("Authorization", `Bearer ${init.token}`);

  const response = await fetch(`${API_BASE_URL}${path}`, { ...init, headers });
  const payload = (await response.json()) as ApiResponse<T>;

  if (!response.ok || !payload.success) {
    throw new Error(payload.error ?? `API request failed with status ${response.status}`);
  }

  return payload.data;
}
