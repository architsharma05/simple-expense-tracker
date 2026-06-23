"use client";

import type { ReactNode } from "react";
import { useEffect, useMemo, useState } from "react";
import { Bar, BarChart, CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { apiFetch, AiAnswer, AiInsight, AuthResponse, Budget, CategorySummary, MonthlySummary, MonthlyTrend, Transaction, TransactionType, User } from "@/lib/api";

const money = new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" });
const currentMonth = new Date().toISOString().slice(0, 7);
const today = new Date().toISOString().slice(0, 10);

type TransactionForm = { type: TransactionType; category: string; amount: string; description: string; transactionDate: string };
type BudgetForm = { category: string; monthlyLimit: string };

export default function HomePage() {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<User | null>(null);
  const [email, setEmail] = useState("demo@example.com");
  const [password, setPassword] = useState("password123");
  const [message, setMessage] = useState<string | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [budgets, setBudgets] = useState<Budget[]>([]);
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
    const [nextTransactions, nextBudgets, nextSummary, nextCategories, nextTrends, nextInsights] = await Promise.all([
      apiFetch<Transaction[]>(`/api/transactions?${params.toString()}`, { token: activeToken }),
      apiFetch<Budget[]>("/api/budgets", { token: activeToken }),
      apiFetch<MonthlySummary>(`/api/dashboard/summary?month=${currentMonth}`, { token: activeToken }),
      apiFetch<CategorySummary[]>(`/api/dashboard/category-summary?from=${currentMonth}-01&to=${today}`, { token: activeToken }),
      apiFetch<MonthlyTrend[]>(`/api/dashboard/monthly-trends?year=${new Date().getFullYear()}`, { token: activeToken }),
      apiFetch<AiInsight[]>("/api/ai/insights", { token: activeToken })
    ]);
    setTransactions(nextTransactions);
    setBudgets(nextBudgets);
    setSummary(nextSummary);
    setCategories(nextCategories);
    setTrends(nextTrends);
    setAiInsights(nextInsights);
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

  async function runAi(kind: "summary" | "coach" | "chat") {
    if (!token) return;
    const path = kind === "summary" ? "/api/ai/spending-summary" : kind === "coach" ? "/api/ai/budget-coach" : "/api/ai/chat";
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
  }

  if (!user) {
    return (
      <main className="min-h-screen bg-slate-950 text-white">
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
                <button className="rounded-xl bg-emerald-600 p-3 font-semibold text-white" onClick={() => authenticate("login")}>Log in</button>
                <button className="rounded-xl border p-3 font-semibold" onClick={() => authenticate("register")}>Register</button>
              </div>
            </div>
          </div>
        </section>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-slate-100 text-slate-950">
      <header className="border-b bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <div><p className="text-sm text-slate-500">Signed in as</p><h1 className="font-semibold">{user.email}</h1></div>
          <button className="rounded-xl border px-4 py-2" onClick={logout}>Log out</button>
        </div>
      </header>
      <section className="mx-auto max-w-7xl space-y-6 px-6 py-8">
        <div className="grid gap-4 md:grid-cols-3">
          <Metric label="Income" value={money.format(Number(summary?.income ?? 0))} />
          <Metric label="Expenses" value={money.format(Number(summary?.expenses ?? 0))} />
          <Metric label="Net" value={netWorthLabel} />
        </div>

        <div className="grid gap-6 lg:grid-cols-[1fr_0.8fr]">
          <Panel title="Add transaction">
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
              <select className="rounded-xl border p-3" value={transactionForm.type} onChange={(e) => setTransactionForm({ ...transactionForm, type: e.target.value as TransactionType })}><option>EXPENSE</option><option>INCOME</option></select>
              <input className="rounded-xl border p-3" value={transactionForm.category} onChange={(e) => setTransactionForm({ ...transactionForm, category: e.target.value })} placeholder="Category" />
              <input className="rounded-xl border p-3" value={transactionForm.amount} onChange={(e) => setTransactionForm({ ...transactionForm, amount: e.target.value })} placeholder="Amount" />
              <input className="rounded-xl border p-3" type="date" value={transactionForm.transactionDate} onChange={(e) => setTransactionForm({ ...transactionForm, transactionDate: e.target.value })} />
              <button className="rounded-xl bg-emerald-600 p-3 font-semibold text-white" onClick={saveTransaction}>{editingTransactionId ? "Update" : "Add"}</button>
            </div>
            <input className="mt-3 w-full rounded-xl border p-3" value={transactionForm.description} onChange={(e) => setTransactionForm({ ...transactionForm, description: e.target.value })} placeholder="Description" />
          </Panel>

          <Panel title="Budget management">
            <div className="grid gap-3 sm:grid-cols-[1fr_1fr_auto]">
              <input className="rounded-xl border p-3" value={budgetForm.category} onChange={(e) => setBudgetForm({ ...budgetForm, category: e.target.value })} placeholder="Category" />
              <input className="rounded-xl border p-3" value={budgetForm.monthlyLimit} onChange={(e) => setBudgetForm({ ...budgetForm, monthlyLimit: e.target.value })} placeholder="Monthly limit" />
              <button className="rounded-xl bg-slate-900 p-3 font-semibold text-white" onClick={addBudget}>Save</button>
            </div>
            <div className="mt-4 space-y-2">{budgets.map((budget) => <Row key={budget.id} label={budget.category} value={money.format(Number(budget.monthlyLimit))} onDelete={() => deleteBudget(budget.id)} />)}</div>
          </Panel>
        </div>

        <div className="grid gap-6 lg:grid-cols-2">
          <Panel title="Monthly trends"><Chart data={trends} kind="line" /></Panel>
          <Panel title="Category summary"><Chart data={categories} kind="bar" /></Panel>
        </div>

        <Panel title="AI Copilot">
          <div className="grid gap-3 md:grid-cols-[1fr_auto_auto]">
            <input className="rounded-xl border p-3" value={aiQuestion} onChange={(e) => setAiQuestion(e.target.value)} placeholder="Ask about your finances" />
            <button className="rounded-xl bg-purple-700 p-3 font-semibold text-white" onClick={() => runAi("chat")}>Ask AI</button>
            <button className="rounded-xl border p-3 font-semibold" onClick={() => runAi("summary")}>Spending summary</button>
          </div>
          <button className="mt-3 rounded-xl border p-3 font-semibold" onClick={() => runAi("coach")}>Budget coach</button>
          {aiAnswer && <div className="mt-4 rounded-2xl bg-purple-50 p-4 text-sm leading-6 whitespace-pre-wrap"><p className="mb-2 font-semibold">{aiAnswer.generatedByAi ? "AI-generated" : "Preview mode"}</p>{aiAnswer.answer}</div>}
          <div className="mt-4 space-y-2">{aiInsights.slice(0, 5).map((insight) => <div key={insight.id} className="rounded-xl border p-3 text-sm text-slate-700">{insight.insightText}</div>)}</div>
        </Panel>

        <Panel title="Transactions">
          <div className="mb-4 grid gap-3 md:grid-cols-4">
            <input className="rounded-xl border p-3" placeholder="Search" value={filters.search} onChange={(e) => setFilters({ ...filters, search: e.target.value })} />
            <input className="rounded-xl border p-3" placeholder="Category" value={filters.category} onChange={(e) => setFilters({ ...filters, category: e.target.value })} />
            <select className="rounded-xl border p-3" value={filters.type} onChange={(e) => setFilters({ ...filters, type: e.target.value })}><option value="">All types</option><option value="EXPENSE">Expenses</option><option value="INCOME">Income</option></select>
            <button className="rounded-xl border p-3 font-semibold" onClick={() => refreshData()}>Apply filters</button>
          </div>
          <div className="overflow-hidden rounded-2xl border">{transactions.map((transaction) => <Row key={transaction.id} label={`${transaction.transactionDate} · ${transaction.category} · ${transaction.description ?? "No description"}`} value={`${transaction.type === "EXPENSE" ? "-" : "+"}${money.format(Number(transaction.amount))}`} onEdit={() => editTransaction(transaction)} onDelete={() => deleteTransaction(transaction.id)} />)}</div>
        </Panel>
      </section>
    </main>
  );
}

function Metric({ label, value }: { label: string; value: string }) { return <div className="rounded-3xl bg-white p-6 shadow-sm"><p className="text-sm text-slate-500">{label}</p><p className="mt-2 text-3xl font-bold">{value}</p></div>; }
function Panel({ title, children }: { title: string; children: ReactNode }) { return <section className="rounded-3xl bg-white p-6 shadow-sm"><h2 className="mb-4 text-xl font-semibold">{title}</h2>{children}</section>; }
function Row({ label, value, onDelete, onEdit }: { label: string; value: string; onDelete: () => void; onEdit?: () => void }) { return <div className="flex items-center justify-between gap-4 border-b p-3 last:border-b-0"><span className="text-sm">{label}</span><div className="flex items-center gap-3"><strong>{value}</strong>{onEdit && <button className="text-sm text-blue-600" onClick={onEdit}>Edit</button>}<button className="text-sm text-red-600" onClick={onDelete}>Delete</button></div></div>; }
function Chart({ data, kind }: { data: Array<MonthlyTrend | CategorySummary>; kind: "line" | "bar" }) {
  const chartData = data.map((item) => "month" in item ? { ...item, month: item.month.toString() } : item);
  return <div className="h-72"><ResponsiveContainer width="100%" height="100%">{kind === "line" ? <LineChart data={chartData}><CartesianGrid strokeDasharray="3 3" /><XAxis dataKey="month" /><YAxis /><Tooltip /><Legend /><Line type="monotone" dataKey="income" stroke="#059669" /><Line type="monotone" dataKey="expenses" stroke="#dc2626" /></LineChart> : <BarChart data={chartData}><CartesianGrid strokeDasharray="3 3" /><XAxis dataKey="category" /><YAxis /><Tooltip /><Bar dataKey="total" fill="#059669" /></BarChart>}</ResponsiveContainer></div>;
}
