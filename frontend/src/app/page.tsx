export default function HomePage() {
  return (
    <main className="min-h-screen bg-background text-foreground">
      <section className="mx-auto flex min-h-screen w-full max-w-6xl flex-col justify-center px-6 py-12">
        <p className="text-sm font-medium uppercase tracking-wide text-primary">
          Phase 0 Foundation
        </p>
        <h1 className="mt-3 max-w-3xl text-4xl font-semibold leading-tight sm:text-5xl">
          AI Finance Copilot
        </h1>
        <p className="mt-5 max-w-2xl text-lg text-muted">
          A modern full-stack personal finance platform built from a Java Swing expense tracker.
        </p>
        <div className="mt-8 grid gap-4 sm:grid-cols-3">
          <div className="border border-border p-4">
            <h2 className="font-medium">Track</h2>
            <p className="mt-2 text-sm text-muted">Expenses, income, budgets, and goals.</p>
          </div>
          <div className="border border-border p-4">
            <h2 className="font-medium">Analyze</h2>
            <p className="mt-2 text-sm text-muted">Monthly summaries, categories, and trends.</p>
          </div>
          <div className="border border-border p-4">
            <h2 className="font-medium">Advise</h2>
            <p className="mt-2 text-sm text-muted">AI insights, coaching, and forecasting.</p>
          </div>
        </div>
      </section>
    </main>
  );
}

