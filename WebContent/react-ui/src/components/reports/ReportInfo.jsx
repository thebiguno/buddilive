import { useState, useRef, useEffect } from 'react';
import { HelpCircle } from 'lucide-react';
import { cn } from '../../lib/utils';
import { useApp } from '../../context/AppContext';

export const REPORT_DESCRIPTIONS = {
  'pie-income': {
    title: 'Income by Category (Pie)',
    body: `Shows the proportion of total income contributed by each top-level income category over the selected period.

Calculation: All transaction splits where the source is an Income category are summed. Each category's total is expressed as a percentage of overall income. Only top-level categories are shown; sub-categories are rolled up into their parent.

Assumptions: Transfers between accounts are excluded. Only splits with an Income-type source are counted.`,
  },
  'pie-expenses': {
    title: 'Expenses by Category (Pie)',
    body: `Shows the proportion of total spending attributed to each top-level expense category over the selected period.

Calculation: All transaction splits where the destination is an Expense category are summed. Each category's total is expressed as a percentage of overall spending. Sub-categories are rolled up into their parent.

Assumptions: Transfers between accounts are excluded. Only splits with an Expense-type destination are counted.`,
  },
  'income-expenses': {
    title: 'Income & Expenses by Category',
    body: `A detailed table comparing income and expense totals for each category over the selected period.

Calculation: Splits are grouped by their category. Income categories show total inflows; expense categories show total outflows. Rows can be expanded to show individual transactions.

Assumptions: Only the current period is shown. Transfers between accounts do not appear.`,
  },
  'avg-income-expenses': {
    title: 'Average Income & Expenses by Category',
    body: `Shows the average monthly income and expense amount per category over the selected period.

Calculation: The same totals as Income & Expenses by Category are divided by the number of months in the selected interval to produce a per-month average.

Assumptions: Partial months at the start or end of the interval are counted as full months. Useful for identifying your typical monthly spending pattern.`,
  },
  'inflow-account': {
    title: 'Inflow & Outflow by Account',
    body: `Shows total money flowing into and out of each account over the selected period.

Calculation: For each account, all splits where that account is the destination are summed as inflow; all splits where it is the source are summed as outflow.

Assumptions: Transfers between your own accounts appear as both an inflow to one account and an outflow from another, so they cancel out at the net worth level.`,
  },
  'inflow-payee': {
    title: 'Inflow & Outflow by Payee',
    body: `Ranks payees (transaction descriptions) by their total inflow and outflow over the selected period.

Calculation: Transactions are grouped by description (payee name). All splits within each transaction are summed as inflow or outflow depending on sign.

Assumptions: Payee matching is exact — the same payee with slightly different spelling will appear as separate entries. Encrypted names are decrypted for display only.`,
  },
  'balances-over-time': {
    title: 'Account Balances Over Time',
    body: `Plots the running balance of each account over the selected period as a line chart.

Calculation: The server replays all transactions in date order, accumulating a running balance for each account. One data point is emitted per week (or on significant dates).

Assumptions: Balances are shown as stored — debit accounts (chequing, savings) are positive; credit accounts (credit cards, loans) may be negative. The starting balance for each account is included.`,
  },
  'net-worth': {
    title: 'Net Worth Over Time',
    body: `Plots your total net worth (sum of all account balances) over the selected period as a single line.

Calculation: Same as Account Balances Over Time, but all account balances are summed into a single Net Worth value at each weekly data point.

Assumptions: All accounts are included — assets (chequing, savings, investments) add to net worth; liabilities (credit cards, loans) subtract. Accounts with no transactions in the period still contribute their balance.`,
  },
  'budget-vs-actual': {
    title: 'Budget vs Actual',
    body: `Compares your budgeted amount against actual spending for each expense category over the selected period.

Calculation: Budget amounts come from the category budget entries for the period. Actual amounts are the sum of all transaction splits assigned to each category. Both are shown as side-by-side bars.

Assumptions: Only expense categories with a budget entry or actual transactions are shown. Income categories are excluded. If a category has no budget set, its budgeted bar will be zero.`,
  },
  'monthly-cash-flow': {
    title: 'Monthly Cash Flow',
    body: `Shows total income, total expenses, and net cash flow (income minus expenses) for each month in the selected period.

Calculation: Splits are grouped by calendar month. Income splits (from an Income-type category) are summed as income; expense splits (to an Expense-type category) are summed as expenses. Net = Income − Expenses.

Assumptions: Transfers between accounts are excluded. A positive Net bar means you spent less than you earned that month; negative means you overspent.`,
  },
  'savings-rate': {
    title: 'Savings Rate',
    body: `Shows what percentage of your income you saved each month over the selected period.

Calculation: Savings Rate % = (Income − Expenses) / Income × 100, computed per calendar month. Plotted as a line chart.

Assumptions: Months with zero income are skipped to avoid division by zero. A rate above 0% means you saved money; below 0% means you spent more than you earned. Transfers between accounts are excluded.`,
  },
  'year-over-year': {
    title: 'Side by Side Period Comparison',
    body: `Compares spending and income by category between two consecutive periods of the same length (e.g. this year vs last year).

Calculation: The selected interval defines the "current" period. The server automatically calculates the equivalent prior period of the same length. Both periods' category totals are shown as side-by-side bars.

Assumptions: The prior period is always the same duration as the selected period, shifted back by exactly one period length. Categories with no activity in either period are excluded.`,
  },
  'top-payees': {
    title: 'Top Payees by Spend',
    body: `Ranks your top payees (transaction descriptions) by total spending over the selected period, shown as a horizontal bar chart.

Calculation: All expense splits are grouped by transaction description (payee). Totals are sorted descending. Each bar also shows the payee's percentage of total spend for the period.

Assumptions: Only outflows to Expense-type categories are counted — income and transfers are excluded. Payee names must match exactly; slight spelling differences create separate entries.`,
  },
  'category-drilldown': {
    title: 'Category Drill-Down',
    body: `Shows month-by-month spending trend for a single selected category over the chosen interval.

Calculation: All transaction splits assigned to the selected category (or any of its sub-categories) are summed per calendar month and plotted as a line chart.

Assumptions: Sub-categories are included in the total. Only the selected category is shown — use multiple tabs to compare categories side by side.`,
  },
  'projected-balance': {
    title: 'Projected Balance',
    body: `Forecasts how your account balances will change over the next N days based on your historical spending patterns.

Calculation: The last 12 months of transactions are analysed to compute an average monthly net change for each account. Starting from today's actual balance, that average is applied on the 1st of each projected month. A weekly snapshot is plotted for each active account.

Assumptions: Only accounts with an average monthly change greater than $1 are shown (static investment accounts are hidden). The projection assumes the future will look like the past 12-month average — it does not account for one-off large transactions, seasonal variation, or changes in spending habits.`,
  },
  'debt-paydown': {
    title: 'Debt Paydown Tracker',
    body: `Tracks the balance of your credit and loan accounts over time to visualise debt reduction progress.

Calculation: All transaction splits involving Credit-type accounts are replayed in date order to produce a running balance for each credit account. One data point is emitted per week.

Assumptions: Only Credit-type accounts (credit cards, lines of credit, loans) are shown. Balances are shown as negative numbers — a line trending toward zero means debt is being paid down. Debit accounts (chequing, savings) are excluded.`,
  },
};

export function ReportInfoButton({ reportType }) {
  const { t } = useApp();
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  const info = REPORT_DESCRIPTIONS[reportType];
  const title = info ? t(`REPORT_INFO_${reportType.toUpperCase().replace(/-/g, '_')}_TITLE`, info.title) : '';
  const body = info ? t(`REPORT_INFO_${reportType.toUpperCase().replace(/-/g, '_')}_BODY`, info.body) : '';

  useEffect(() => {
    function handler(e) {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    }
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  if (!info) return null;

  return (
    <div ref={ref} className="relative">
      <button
        onClick={() => setOpen(v => !v)}
        title={t('ABOUT_THIS_REPORT', 'About this report')}
        className={cn(
          'inline-flex items-center gap-1 px-2 py-1 rounded border border-transparent text-xs',
          'hover:bg-white/60 hover:border-gray-400 active:bg-gray-200 transition-colors cursor-pointer',
          open && 'bg-white/60 border-gray-400'
        )}
      >
        <HelpCircle size={13} />
      </button>
      {open && (
        <div className="absolute left-0 top-full mt-1 z-50 bg-white border border-gray-300 shadow-xl rounded w-80 p-3">
          <div className="text-xs font-semibold text-gray-800 mb-2">{title}</div>
          <div className="text-xs text-gray-600 whitespace-pre-wrap leading-relaxed">{body}</div>
        </div>
      )}
    </div>
  );
}
