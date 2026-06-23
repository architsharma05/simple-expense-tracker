"use client";

import type { ReactNode } from "react";
import { useEffect, useMemo, useState } from "react";
import { Bar, BarChart, CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { apiFetch, AiAnswer, AiInsight, Anomaly, AuthResponse, Budget, CategoryForecast, CategorySuggestion, CategorySummary, Goal, MonthEndForecast, MonthlySummary, MonthlyTrend, Receipt, ReceiptExtraction, RecurringExpense, Transaction, TransactionType, User } from "@/lib/api";

const money = new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" });
const currentMonth = new Date().toISOString().slice(0, 7);
const today = new Date().toISOString().slice(0, 10);

type TransactionForm = { type: TransactionType; category: string; amount: string; description: string; transactionDate: string };
type BudgetForm = { category: string; monthlyLimit: string };
type GoalForm = { goalName: string; targetAmount: string; targetDate: string };

export default function HomePage() {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<User | null>(null);
  const [email, setEmail] = useState("demo@example.com");
  const [password, setPassword] = useState("password123");
  const [message, setMessage] = useState<string | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [budgets, setBudgets] = useState<Budget[]>([]);
  const [goals, setGoals] = useState<Goal[]>([]);
  const [recurringExpenses, setRecurringExpenses] = useState<RecurringExpense[]>([]);
  const [anomalies, setAnomalies] = useState<Anomaly[]>([]);
  const [monthEndForecast, setMonthEndForecast] = useState<MonthEndForecast | null>(null);
  const [categoryForecasts, setCategoryForecasts] = useState<CategoryForecast[]>([]);
  const [categorySuggestion, setCategorySuggestion] = useState<CategorySuggestion | null>(null);
  const [receipts, setReceipts] = useState<Receipt[]>([]);
  const [receiptExtraction, setReceiptExtraction] = useState<ReceiptExtraction | null>(null);
  const [summary, setSummary] = useState<MonthlySummary | null>(null);
  const [categories, setCategories] = useState<CategorySummary[]>([]);
  const [trends, setTrends] = useState<MonthlyTrend[]>([]);
  const [aiAnswer, setAiAnswer] = useState<AiAnswer | null>(null);
  const [aiInsights, setAiInsights] = useState<AiInsight[]>([]);
  const [aiQuestion, setAiQuestion] = useState("Where did I spend the most money this month?");
  const [filters, setFilters] = useState({ search: "", category: "", type: "" });
  const [editingTransactionId, setEditingTransactionId] = useState<string | null>(null);
  const [transactionForm, setTransactionForm] = useState<TransactionForm>({ type: "EXPENSE", category: "Food", amount: "25.00", description: "", transactionDate: today });
  const [budgetForm, setBudgetForm] = useState<BudgetForm>({ category: "Food", monthlyLimit: "500.00" });
  const [goalForm, setGoalForm] = useState<GoalForm>({ goalName: "Emergency fund", targetAmount: "5000.00", targetDate: new Date(Date.now() + 180 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10) });

  useEffect(() => {
    const savedToken = window.localStorage.getItem("finance-token");
    if (savedToken) {
      setToken(savedToken);
      apiFetch<User>("/api/auth/me", { token: savedToken }).then(setUser).catch(() => window.localStorage.removeItem("finance-token"));
    }
  }, []);

  useEffect(() => {
    if (token) void refreshData(token);
  }, [token]);

  const netWorthLabel = useMemo(() => (summary ? money.format(Number(summary.net)) : money.format(0)), [summary]);

  async function authenticate(mode: "login" | "register") {
    setMessage(null);
    try {
      const auth = await apiFetch<AuthResponse>(`/api/auth/${mode}`, { method: "POST", body: JSON.stringify({ email, password }) });
      window.localStorage.setItem("finance-token", auth.accessToken);
      setToken(auth.accessToken);
      setUser(auth.user);
      setMessage(mode === "login" ? "Logged in successfully." : "Account created successfully.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Authentication failed.");
    }
  }

  async function refreshData(activeToken = token) {
    if (!activeToken) return;
    const params = new URLSearchParams();
    if (filters.search) params.set("search", filters.search);
    if (filters.category) params.set("category", filters.category);
    if (filters.type) params.set("type", filters.type);
    const [nextTransactions, nextBudgets, nextGoals, nextRecurring, nextAnomalies, nextMonthEndForecast, nextCategoryForecasts, nextReceipts, nextSummary, nextCategories, nextTrends, nextInsights] = await Promise.all([
      apiFetch<Transaction[]>(`/api/transactions?${params.toString()}`, { token: activeToken }),
      apiFetch<Budget[]>("/api/budgets", { token: activeToken }),
      apiFetch<Goal[]>("/api/goals", { token: activeToken }),
      apiFetch<RecurringExpense[]>("/api/insights/recurring-expenses", { token: activeToken }),
      apiFetch<Anomaly[]>("/api/insights/anomalies", { token: activeToken }),
      apiFetch<MonthEndForecast>("/api/forecast/month-end", { token: activeToken }),
      apiFetch<CategoryForecast[]>("/api/forecast/categories", { token: activeToken }),
      apiFetch<Receipt[]>("/api/receipts", { token: activeToken }),
      apiFetch<MonthlySummary>(`/api/dashboard/summary?month=${currentMonth}`, { token: activeToken }),
      apiFetch<CategorySummary[]>(`/api/dashboard/category-summary?from=${currentMonth}-01&to=${today}`, { token: activeToken }),
      apiFetch<MonthlyTrend[]>(`/api/dashboard/monthly-trends?year=${new Date().getFullYear()}`, { token: activeToken }),
      apiFetch<AiInsight[]>("/api/ai/insights", { token: activeToken })
    ]);
    setTransactions(nextTransactions);
    setBudgets(nextBudgets);
    setGoals(nextGoals);
    setRecurringExpenses(nextRecurring);
    setAnomalies(nextAnomalies);
    setMonthEndForecast(nextMonthEndForecast);
    setCategoryForecasts(nextCategoryForecasts);
    setReceipts(nextReceipts);
    setSummary(nextSummary);
    setCategories(nextCategories);
    setTrends(nextTrends);
    setAiInsights(nextInsights);
  }

  async function suggestCategory() {
    if (!token || !transactionForm.description || !transactionForm.amount) return;
    const suggestion = await apiFetch<CategorySuggestion>("/api/ai/categorize-transaction", {
      method: "POST",
      token,
      body: JSON.stringify({ type: transactionForm.type, amount: Number(transactionForm.amount), description: transactionForm.description })
    });
    setCategorySuggestion(suggestion);
    setTransactionForm({ ...transactionForm, category: suggestion.category });
  }

  async function saveTransaction() {
    if (!token) return;
    await apiFetch<Transaction>(editingTransactionId ? `/api/transactions/${editingTransactionId}` : "/api/transactions", {
      method: editingTransactionId ? "PUT" : "POST",
      token,
      body: JSON.stringify({ ...transactionForm, amount: Number(transactionForm.amount) })
    });
    setEditingTransactionId(null);
    setTransactionForm({ ...transactionForm, amount: "", description: "" });
    await refreshData();
  }

  function editTransaction(transaction: Transaction) {
    setEditingTransactionId(transaction.id);
    setTransactionForm({
      type: transaction.type,
      category: transaction.category,
      amount: String(transaction.amount),
      description: transaction.description ?? "",
      transactionDate: transaction.transactionDate
    });
  }

  async function deleteTransaction(id: string) {
    if (!token) return;
    await apiFetch<null>(`/api/transactions/${id}`, { method: "DELETE", token });
    await refreshData();
  }

  async function addBudget() {
    if (!token) return;
    await apiFetch<Budget>("/api/budgets", { method: "POST", token, body: JSON.stringify({ category: budgetForm.category, monthlyLimit: Number(budgetForm.monthlyLimit) }) });
    setBudgetForm({ category: "", monthlyLimit: "" });
    await refreshData();
  }

  async function deleteBudget(id: string) {
    if (!token) return;
    await apiFetch<null>(`/api/budgets/${id}`, { method: "DELETE", token });
    await refreshData();
  }

  async function addGoal() {
    if (!token) return;
    await apiFetch<Goal>("/api/goals", { method: "POST", token, body: JSON.stringify({ goalName: goalForm.goalName, targetAmount: Number(goalForm.targetAmount), targetDate: goalForm.targetDate }) });
    setGoalForm({ ...goalForm, goalName: "", targetAmount: "" });
    await refreshData();
  }

  async function deleteGoal(id: string) {
    if (!token) return;
    await apiFetch<null>(`/api/goals/${id}`, { method: "DELETE", token });
    await refreshData();
  }

  async function uploadReceipt(file: File | null) {
    if (!token || !file) return;
    const body = new FormData();
    body.append("file", file);
    await apiFetch<Receipt>("/api/receipts/upload", { method: "POST", token, body });
    await refreshData();
  }

  async function extractReceipt(id: string) {
    if (!token) return;
    const extraction = await apiFetch<ReceiptExtraction>(`/api/receipts/${id}/extract`, { method: "POST", token });
    setReceiptExtraction(extraction);
  }

  async function deleteReceipt(id: string) {
    if (!token) return;
    await apiFetch<null>(`/api/receipts/${id}`, { method: "DELETE", token });
    await refreshData();
  }

  async function runAi(kind: "summary" | "coach" | "goal" | "chat") {
    if (!token) return;
    const path = kind === "summary" ? "/api/ai/spending-summary" : kind === "coach" ? "/api/ai/budget-coach" : kind === "goal" ? "/api/ai/goal-coach" : "/api/ai/chat";
    const answer = await apiFetch<AiAnswer>(path, {
      method: "POST",
      token,
      body: kind === "chat" ? JSON.stringify({ question: aiQuestion }) : undefined
    });
    setAiAnswer(answer);
    await refreshData();
  }

  function logout() {
    window.localStorage.removeItem("finance-token");
    setToken(null);
    setUser(null);
    setTransactions([]);
    setBudgets([]);
    setGoals([]);
    setReceipts([]);
  }

  if (!user) {
    return (
      <main className="min-h-screen bg-black text-white">
        <section className="mx-auto grid min-h-screen max-w-6xl gap-10 px-6 py-16 lg:grid-cols-[1.2fr_0.8fr] lg:items-center">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.3em] text-emerald-300">AI Finance Copilot</p>
            <h1 className="mt-4 text-5xl font-bold tracking-tight">Phase 1 finance management MVP</h1>
            <p className="mt-5 max-w-2xl text-lg text-slate-300">Securely track income, expenses, budgets, monthly summaries, category breakdowns, and trends before layering in AI insights.</p>
          </div>
          <div className="rounded-3xl border border-white/10 bg-white p-6 text-slate-950 shadow-2xl">
            <h2 className="text-2xl font-semibold">Access your workspace</h2>
            <div className="mt-5 space-y-3">
              <input className="w-full rounded-xl border p-3" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="Email" />
              <input className="w-full rounded-xl border p-3" type="password" value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Password" />
              {message && <p className="text-sm text-red-600">{message}</p>}
              <div className="grid grid-cols-2 gap-3">
                <button className="rounded-xl bg-blue-600 p-3 font-semibold text-white shadow-lg shadow-blue-600/20 transition hover:bg-blue-500 active:scale-[0.99]" onClick={() => authenticate("login")}>Log in</button>
                <button className="rounded-xl border p-3 font-semibold" onClick={() => authenticate("register")}>Register</button>
              </div>
            </div>
          </div>
        </section>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-black text-slate-50">
      <header className="sticky top-0 z-20 border-b border-blue-500/20 bg-black/80 backdrop-blur">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <div><p className="text-sm text-slate-500">Signed in as</p><h1 className="font-semibold">{user.email}</h1></div>
          <button className="rounded-xl border px-4 py-2" onClick={logout}>Log out</button>
        </div>
      </header>
      <section className="mx-auto max-w-7xl space-y-6 px-6 py-8">
        <div className="grid gap-4 md:grid-cols-4">
          <Metric label="Income" value={money.format(Number(summary?.income ?? 0))} />
          <Metric label="Expenses" value={money.format(Number(summary?.expenses ?? 0))} />
          <Metric label="Net" value={netWorthLabel} />
          <Metric label="Projected spend" value={money.format(Number(monthEndForecast?.projectedSpend ?? 0))} />
        </div>

        <div className="grid gap-6 lg:grid-cols-[1fr_0.8fr]">
          <Panel title="Add transaction">
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
              <select className="rounded-xl border border-blue-500/30 p-3 text-slate-950" value={transactionForm.type} onChange={(e) => setTransactionForm({ ...transactionForm, type: e.target.value as TransactionType })}><option>EXPENSE</option><option>INCOME</option></select>
              <input className="rounded-xl border border-blue-500/30 p-3 text-slate-950" value={transactionForm.category} onChange={(e) => setTransactionForm({ ...transactionForm, category: e.target.value })} placeholder="Category" />
              <input className="rounded-xl border border-blue-500/30 p-3 text-slate-950" value={transactionForm.amount} onChange={(e) => setTransactionForm({ ...transactionForm, amount: e.target.value })} placeholder="Amount" />
              <input className="rounded-xl border border-blue-500/30 p-3 text-slate-950" type="date" value={transactionForm.transactionDate} onChange={(e) => setTransactionForm({ ...transactionForm, transactionDate: e.target.value })} />
              <button className="rounded-xl bg-blue-600 p-3 font-semibold text-white shadow-lg shadow-blue-600/20 transition hover:bg-blue-500 active:scale-[0.99]" onClick={saveTransaction}>{editingTransactionId ? "Update" : "Add"}</button>
            </div>
            <div className="mt-3 grid gap-3 md:grid-cols-[1fr_auto]"><input className="w-full rounded-xl border p-3 text-slate-950" value={transactionForm.description} onChange={(e) => setTransactionForm({ ...transactionForm, description: e.target.value })} placeholder="Description" /><button className="rounded-xl border border-blue-500/40 p-3 font-semibold text-blue-100" onClick={suggestCategory}>Suggest category</button></div>{categorySuggestion && <p className="mt-2 text-sm text-blue-200">Suggested {categorySuggestion.category} · {Math.round(categorySuggestion.confidence * 100)}% confidence</p>}
          </Panel>

          <Panel title="Budget management">
            <div className="grid gap-3 sm:grid-cols-[1fr_1fr_auto]">
              <input className="rounded-xl border border-blue-500/30 p-3 text-slate-950" value={budgetForm.category} onChange={(e) => setBudgetForm({ ...budgetForm, category: e.target.value })} placeholder="Category" />
              <input className="rounded-xl border border-blue-500/30 p-3 text-slate-950" value={budgetForm.monthlyLimit} onChange={(e) => setBudgetForm({ ...budgetForm, monthlyLimit: e.target.value })} placeholder="Monthly limit" />
              <button className="rounded-xl bg-blue-700 p-3 font-semibold text-white shadow-lg shadow-blue-600/20 transition hover:bg-blue-500 active:scale-[0.99]" onClick={addBudget}>Save</button>
            </div>
            <div className="mt-4 space-y-2">{budgets.map((budget) => <Row key={budget.id} label={budget.category} value={money.format(Number(budget.monthlyLimit))} onDelete={() => deleteBudget(budget.id)} />)}</div>
          </Panel>

          <Panel title="Goal tracking">
            <div className="grid gap-3 sm:grid-cols-[1fr_1fr_1fr_auto]">
              <input className="rounded-xl border border-blue-500/30 p-3 text-slate-950" value={goalForm.goalName} onChange={(e) => setGoalForm({ ...goalForm, goalName: e.target.value })} placeholder="Goal" />
              <input className="rounded-xl border border-blue-500/30 p-3 text-slate-950" value={goalForm.targetAmount} onChange={(e) => setGoalForm({ ...goalForm, targetAmount: e.target.value })} placeholder="Target amount" />
              <input className="rounded-xl border border-blue-500/30 p-3 text-slate-950" type="date" value={goalForm.targetDate} onChange={(e) => setGoalForm({ ...goalForm, targetDate: e.target.value })} />
              <button className="rounded-xl bg-blue-700 p-3 font-semibold text-white shadow-lg shadow-blue-600/20 transition hover:bg-blue-500 active:scale-[0.99]" onClick={addGoal}>Add</button>
            </div>
            <div className="mt-4 space-y-2">{goals.map((goal) => <Row key={goal.id} label={`${goal.goalName} · ${goal.daysRemaining} days left`} value={`${money.format(Number(goal.targetAmount))} target · ${money.format(Number(goal.monthlySavingsRequired))}/mo`} onDelete={() => deleteGoal(goal.id)} />)}</div>
          </Panel>
        </div>

        <div className="grid gap-6 lg:grid-cols-2">
          <Panel title="Monthly trends"><Chart data={trends} kind="line" /></Panel>
          <Panel title="Category summary"><Chart data={categories} kind="bar" /></Panel>
        </div>

        <Panel title="AI Copilot">
          <div className="grid gap-3 md:grid-cols-[1fr_auto_auto]">
            <input className="rounded-xl border border-blue-500/30 p-3 text-slate-950" value={aiQuestion} onChange={(e) => setAiQuestion(e.target.value)} placeholder="Ask about your finances" />
            <button className="rounded-xl bg-blue-700 p-3 font-semibold text-white shadow-lg shadow-blue-600/20 transition hover:bg-blue-500 active:scale-[0.99]" onClick={() => runAi("chat")}>Ask AI</button>
            <button className="rounded-xl border p-3 font-semibold" onClick={() => runAi("summary")}>Spending summary</button>
          </div>
          <div className="mt-3 flex flex-wrap gap-3"><button className="rounded-xl border p-3 font-semibold" onClick={() => runAi("coach")}>Budget coach</button><button className="rounded-xl border p-3 font-semibold" onClick={() => runAi("goal")}>Goal coach</button></div>
          {aiAnswer && <div className="mt-4 rounded-2xl bg-purple-50 p-4 text-sm leading-6 whitespace-pre-wrap"><p className="mb-2 font-semibold">{aiAnswer.generatedByAi ? "AI-generated" : "Preview mode"}</p>{aiAnswer.answer}</div>}
          <div className="mt-4 space-y-2">{aiInsights.slice(0, 5).map((insight) => <div key={insight.id} className="rounded-xl border p-3 text-sm text-slate-700">{insight.insightText}</div>)}</div>
        </Panel>

        <div className="grid gap-6 lg:grid-cols-2">
          <Panel title="Recurring expense detection">
            <div className="space-y-2">{recurringExpenses.slice(0, 6).map((item) => <div key={`${item.category}-${item.description}`} className="rounded-xl border p-3 text-sm"><strong>{item.category}</strong> · {money.format(Number(item.typicalAmount))} · {item.occurrenceCount} times</div>)}</div>
          </Panel>
          <Panel title="Anomaly detection">
            <div className="space-y-2">{anomalies.slice(0, 6).map((item) => <div key={item.category} className="rounded-xl border border-blue-500/20 p-3 text-sm"><strong>{item.category}</strong> · {item.explanation}</div>)}</div>
          </Panel>
          <Panel title="Spending forecast">
            <div className="space-y-2">{categoryForecasts.slice(0, 6).map((item) => <div key={item.category} className="rounded-xl border border-blue-500/20 p-3 text-sm"><strong>{item.category}</strong> · projected {money.format(Number(item.projectedSpend))} · {item.riskLevel}</div>)}</div>
          </Panel>
        </div>

        <Panel title="Receipt vault">
          <div className="grid gap-3 md:grid-cols-[1fr_auto]">
            <input className="rounded-xl border border-blue-500/30 bg-white p-3 text-slate-950" type="file" accept="image/png,image/jpeg,image/webp,application/pdf" onChange={(event) => uploadReceipt(event.target.files?.[0] ?? null)} />
            <span className="rounded-xl border border-blue-500/30 p-3 text-sm text-blue-100">Upload receipts now; OCR extraction is scaffolded next.</span>
          </div>
          {receiptExtraction && <div className="mt-3 rounded-xl border border-blue-500/20 p-3 text-sm text-blue-100">{receiptExtraction.status}: {receiptExtraction.notes}</div>}
          <div className="mt-4 space-y-2">{receipts.slice(0, 6).map((receipt) => <div key={receipt.id} className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-blue-500/20 p-3 text-sm"><span>{new Date(receipt.uploadedAt).toLocaleString()} · {receipt.fileUrl}</span><span className="flex gap-2"><button className="text-blue-300" onClick={() => extractReceipt(receipt.id)}>Extract</button><button className="text-red-300" onClick={() => deleteReceipt(receipt.id)}>Delete</button></span></div>)}</div>
        </Panel>

        <Panel title="Transactions">
          <div className="mb-4 grid gap-3 md:grid-cols-4">
            <input className="rounded-xl border border-blue-500/30 p-3 text-slate-950" placeholder="Search" value={filters.search} onChange={(e) => setFilters({ ...filters, search: e.target.value })} />
            <input className="rounded-xl border border-blue-500/30 p-3 text-slate-950" placeholder="Category" value={filters.category} onChange={(e) => setFilters({ ...filters, category: e.target.value })} />
            <select className="rounded-xl border border-blue-500/30 p-3 text-slate-950" value={filters.type} onChange={(e) => setFilters({ ...filters, type: e.target.value })}><option value="">All types</option><option value="EXPENSE">Expenses</option><option value="INCOME">Income</option></select>
            <button className="rounded-xl border p-3 font-semibold" onClick={() => refreshData()}>Apply filters</button>
          </div>
          <div className="overflow-hidden rounded-2xl border">{transactions.map((transaction) => <Row key={transaction.id} label={`${transaction.transactionDate} · ${transaction.category} · ${transaction.description ?? "No description"}`} value={`${transaction.type === "EXPENSE" ? "-" : "+"}${money.format(Number(transaction.amount))}`} onEdit={() => editTransaction(transaction)} onDelete={() => deleteTransaction(transaction.id)} />)}</div>
        </Panel>
      </section>
    </main>
  );
}

function Metric({ label, value }: { label: string; value: string }) { return <div className="rounded-3xl border border-blue-500/20 bg-slate-950 p-6 shadow-xl shadow-blue-950/30"><p className="text-sm text-blue-200">{label}</p><p className="mt-2 text-3xl font-bold text-white">{value}</p></div>; }
function Panel({ title, children }: { title: string; children: ReactNode }) { return <section className="rounded-3xl border border-blue-500/20 bg-slate-950 p-6 shadow-xl shadow-blue-950/30"><h2 className="mb-4 text-xl font-semibold text-white">{title}</h2>{children}</section>; }
function Row({ label, value, onDelete, onEdit }: { label: string; value: string; onDelete: () => void; onEdit?: () => void }) { return <div className="flex items-center justify-between gap-4 border-b p-3 last:border-b-0"><span className="text-sm">{label}</span><div className="flex items-center gap-3"><strong>{value}</strong>{onEdit && <button className="text-sm text-blue-600" onClick={onEdit}>Edit</button>}<button className="text-sm text-red-600" onClick={onDelete}>Delete</button></div></div>; }
function Chart({ data, kind }: { data: Array<MonthlyTrend | CategorySummary>; kind: "line" | "bar" }) {
  const chartData = data.map((item) => "month" in item ? { ...item, month: item.month.toString() } : item);
  return <div className="h-72"><ResponsiveContainer width="100%" height="100%">{kind === "line" ? <LineChart data={chartData}><CartesianGrid strokeDasharray="3 3" /><XAxis dataKey="month" /><YAxis /><Tooltip /><Legend /><Line type="monotone" dataKey="income" stroke="#059669" /><Line type="monotone" dataKey="expenses" stroke="#dc2626" /></LineChart> : <BarChart data={chartData}><CartesianGrid strokeDasharray="3 3" /><XAxis dataKey="category" /><YAxis /><Tooltip /><Bar dataKey="total" fill="#059669" /></BarChart>}</ResponsiveContainer></div>;
}
