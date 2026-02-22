import { useState, useEffect } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  ResponsiveContainer, ReferenceLine, LineChart, Line, Cell
} from 'recharts';
import { api } from '../../lib/api';
import { useApp } from '../../context/AppContext';
import { formatLocaleCurrency, formatLocaleNumber } from '../../lib/numberFormat';

const INCOME_COLOR = '#59a14f';
const EXPENSE_COLOR = '#e15759';
const BUDGET_COLOR = '#4e79a7';
const ACTUAL_COLOR = '#f28e2b';
const NET_POS_COLOR = '#59a14f';
const NET_NEG_COLOR = '#e15759';
const CURRENT_COLOR = '#4e79a7';
const PREVIOUS_COLOR = '#f28e2b';
const SAVINGS_COLOR = '#76b7b2';

function Loading({ t }) {
  return <div className="flex items-center justify-center h-full text-sm text-gray-400">{t('LOADING', 'Loading...')}</div>;
}

function currencyFormatter(value, locale, currency) {
  const n = Number(value);
  if (!Number.isFinite(n)) return '';
  if (Math.abs(n) >= 1000) {
    return formatLocaleCurrency(n, locale, currency, { minimumFractionDigits: 0, maximumFractionDigits: 1, notation: 'compact' });
  }
  return formatLocaleCurrency(n, locale, currency, { minimumFractionDigits: 0, maximumFractionDigits: 0 });
}

// ── Budget vs Actual ──────────────────────────────────────────────────────────

export function BudgetVsActualReport({ options }) {
  const { showError, t, userConfig } = useApp();
  const locale = userConfig?.locale;
  const currency = userConfig?.currency;
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    api.reports.budgetVsActual(options.query)
      .then(res => setData(res?.data || []))
      .catch(showError)
      .finally(() => setLoading(false));
  }, [options.query, showError]);

  if (loading) return <Loading t={t} />;

  const budgetedKey = t('BUDGETED', 'Budgeted');
  const actualKey = t('ACTUAL', 'Actual');

  const chartData = data.map(d => ({
    category: d.category,
    [budgetedKey]: d.budgeted,
    [actualKey]: d.actual,
  }));

  return (
    <div className="h-full w-full p-2 flex flex-col gap-2">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={chartData} margin={{ top: 5, right: 20, left: 20, bottom: 100 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="category" angle={-45} textAnchor="end" interval={0} tick={{ fontSize: 10 }} />
          <YAxis tickFormatter={v => currencyFormatter(v, locale, currency)} tick={{ fontSize: 10 }} />
          <Tooltip formatter={(v) => formatLocaleCurrency(v, locale, currency, { minimumFractionDigits: 2, maximumFractionDigits: 2 })} />
          <Legend />
          <Bar dataKey={budgetedKey} fill={BUDGET_COLOR} />
          <Bar dataKey={actualKey} fill={ACTUAL_COLOR} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

// ── Monthly Cash Flow ─────────────────────────────────────────────────────────

function CashFlowTooltip({ active, payload, label, formatCurrency }) {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-white border border-gray-200 shadow rounded px-3 py-2 text-xs">
      <div className="font-semibold mb-1">{label}</div>
      {payload.map(p => (
        <div key={p.dataKey} style={{ color: p.fill }}>
          {p.dataKey}: {formatCurrency(p.value)}
        </div>
      ))}
    </div>
  );
}

export function MonthlyCashFlowReport({ options }) {
  const { showError, t, userConfig } = useApp();
  const locale = userConfig?.locale;
  const currency = userConfig?.currency;
  const formatCurrency = v => formatLocaleCurrency(v, locale, currency, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    api.reports.monthlyCashFlow(options.query)
      .then(res => setData(res?.data || []))
      .catch(showError)
      .finally(() => setLoading(false));
  }, [options.query, showError]);

  if (loading) return <Loading t={t} />;

  const incomeKey = t('INCOME', 'Income');
  const expensesKey = t('EXPENSES', 'Expenses');
  const netKey = t('NET', 'Net');

  const chartData = data.map(d => ({
    month: d.month,
    [incomeKey]: d.income,
    [expensesKey]: d.expenses,
    [netKey]: d.net,
  }));

  return (
    <div className="h-full w-full p-2">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={chartData} margin={{ top: 5, right: 20, left: 20, bottom: 60 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="month" angle={-45} textAnchor="end" interval={0} tick={{ fontSize: 10 }} />
          <YAxis tickFormatter={v => currencyFormatter(v, locale, currency)} tick={{ fontSize: 10 }} />
          <Tooltip content={<CashFlowTooltip formatCurrency={formatCurrency} />} />
          <Legend />
          <ReferenceLine y={0} stroke="#666" />
          <Bar dataKey={incomeKey} fill={INCOME_COLOR} />
          <Bar dataKey={expensesKey} fill={EXPENSE_COLOR} />
          <Bar dataKey={netKey}>
            {chartData.map((entry, i) => (
              <Cell key={i} fill={entry[netKey] >= 0 ? NET_POS_COLOR : NET_NEG_COLOR} fillOpacity={0.7} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

// ── Savings Rate ──────────────────────────────────────────────────────────────

export function SavingsRateReport({ options }) {
  const { showError, t, userConfig } = useApp();
  const locale = userConfig?.locale;
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    api.reports.savingsRate(options.query)
      .then(res => setData(res?.data || []))
      .catch(showError)
      .finally(() => setLoading(false));
  }, [options.query, showError]);

  if (loading) return <Loading t={t} />;

  const savingsRateKey = t('SAVINGS_RATE_PERCENT', 'Savings Rate %');

  const chartData = data.map(d => ({
    month: d.month,
    [savingsRateKey]: d.savingsRate,
  }));

  return (
    <div className="h-full w-full p-2">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={chartData} margin={{ top: 5, right: 20, left: 20, bottom: 60 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="month" angle={-45} textAnchor="end" interval={0} tick={{ fontSize: 10 }} />
          <YAxis tickFormatter={v => `${formatLocaleNumber(v, locale, { minimumFractionDigits: 0, maximumFractionDigits: 0 })}%`} tick={{ fontSize: 10 }} />
          <Tooltip formatter={(v) => `${formatLocaleNumber(v, locale, { minimumFractionDigits: 1, maximumFractionDigits: 1 })}%`} />
          <Legend />
          <ReferenceLine y={0} stroke="#666" />
          <Line type="monotone" dataKey={savingsRateKey} stroke={SAVINGS_COLOR} strokeWidth={2} dot={{ r: 3 }} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}

// ── Year-over-Year ────────────────────────────────────────────────────────────

export function YearOverYearReport({ options }) {
  const { showError, t, userConfig } = useApp();
  const locale = userConfig?.locale;
  const currency = userConfig?.currency;
  const [data, setData] = useState([]);
  const [meta, setMeta] = useState({});
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    api.reports.yearOverYear(options.query)
      .then(res => {
        setData(res?.data || []);
        setMeta({ currentPeriod: res?.currentPeriod, previousPeriod: res?.previousPeriod });
      })
      .catch(showError)
      .finally(() => setLoading(false));
  }, [options.query, showError]);

  if (loading) return <Loading t={t} />;

  const chartData = data.map(d => ({
    category: d.category,
    [meta.currentPeriod || 'Current']: d.current,
    [meta.previousPeriod || 'Previous']: d.previous,
  }));

  const currentKey = meta.currentPeriod || 'Current';
  const previousKey = meta.previousPeriod || 'Previous';

  return (
    <div className="h-full w-full p-2 flex flex-col gap-1">
      {meta.currentPeriod && (
        <div className="text-xs text-gray-500 text-center">
          {t('COMPARING', 'Comparing')} <span className="font-semibold text-[#4e79a7]">{meta.currentPeriod}</span> {t('VS', 'vs')} <span className="font-semibold text-[#f28e2b]">{meta.previousPeriod}</span>
        </div>
      )}
      <div className="flex-1 min-h-0">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={chartData} margin={{ top: 5, right: 20, left: 20, bottom: 100 }}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="category" angle={-45} textAnchor="end" interval={0} tick={{ fontSize: 10 }} />
            <YAxis tickFormatter={v => currencyFormatter(v, locale, currency)} tick={{ fontSize: 10 }} />
            <Tooltip formatter={(v) => formatLocaleCurrency(v, locale, currency, { minimumFractionDigits: 2, maximumFractionDigits: 2 })} />
            <Legend />
            <Bar dataKey={currentKey} fill={CURRENT_COLOR} />
            <Bar dataKey={previousKey} fill={PREVIOUS_COLOR} />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
