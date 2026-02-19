import { useState, useEffect } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  ResponsiveContainer, ReferenceLine, LineChart, Line, Cell
} from 'recharts';
import { api } from '../../lib/api';
import { useApp } from '../../context/AppContext';

const INCOME_COLOR = '#59a14f';
const EXPENSE_COLOR = '#e15759';
const BUDGET_COLOR = '#4e79a7';
const ACTUAL_COLOR = '#f28e2b';
const NET_POS_COLOR = '#59a14f';
const NET_NEG_COLOR = '#e15759';
const CURRENT_COLOR = '#4e79a7';
const PREVIOUS_COLOR = '#f28e2b';
const SAVINGS_COLOR = '#76b7b2';

function Loading() {
  return <div className="flex items-center justify-center h-full text-sm text-gray-400">Loading...</div>;
}

function currencyFormatter(value) {
  if (Math.abs(value) >= 1000) return `$${(value / 1000).toFixed(1)}k`;
  return `$${value.toFixed(0)}`;
}

// ── Budget vs Actual ──────────────────────────────────────────────────────────

export function BudgetVsActualReport({ options }) {
  const { showError } = useApp();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    api.reports.budgetVsActual(options.query)
      .then(res => setData(res?.data || []))
      .catch(showError)
      .finally(() => setLoading(false));
  }, [options.query, showError]);

  if (loading) return <Loading />;

  const chartData = data.map(d => ({
    category: d.category,
    Budgeted: d.budgeted,
    Actual: d.actual,
  }));

  return (
    <div className="h-full w-full p-2 flex flex-col gap-2">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={chartData} margin={{ top: 5, right: 20, left: 20, bottom: 100 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="category" angle={-45} textAnchor="end" interval={0} tick={{ fontSize: 10 }} />
          <YAxis tickFormatter={currencyFormatter} tick={{ fontSize: 10 }} />
          <Tooltip formatter={(v) => `$${v.toFixed(2)}`} />
          <Legend />
          <Bar dataKey="Budgeted" fill={BUDGET_COLOR} />
          <Bar dataKey="Actual" fill={ACTUAL_COLOR} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

// ── Monthly Cash Flow ─────────────────────────────────────────────────────────

const CASH_FLOW_COLORS = { Expenses: EXPENSE_COLOR, Income: INCOME_COLOR, Net: NET_POS_COLOR };

function CashFlowTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-white border border-gray-200 shadow rounded px-3 py-2 text-xs">
      <div className="font-semibold mb-1">{label}</div>
      {payload.map(p => (
        <div key={p.dataKey} style={{ color: CASH_FLOW_COLORS[p.dataKey] ?? p.fill }}>
          {p.dataKey} : ${Number(p.value).toFixed(2)}
        </div>
      ))}
    </div>
  );
}

export function MonthlyCashFlowReport({ options }) {
  const { showError } = useApp();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    api.reports.monthlyCashFlow(options.query)
      .then(res => setData(res?.data || []))
      .catch(showError)
      .finally(() => setLoading(false));
  }, [options.query, showError]);

  if (loading) return <Loading />;

  const chartData = data.map(d => ({
    month: d.month,
    Income: d.income,
    Expenses: d.expenses,
    Net: d.net,
  }));

  return (
    <div className="h-full w-full p-2">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={chartData} margin={{ top: 5, right: 20, left: 20, bottom: 60 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="month" angle={-45} textAnchor="end" interval={0} tick={{ fontSize: 10 }} />
          <YAxis tickFormatter={currencyFormatter} tick={{ fontSize: 10 }} />
          <Tooltip content={<CashFlowTooltip />} />
          <Legend />
          <ReferenceLine y={0} stroke="#666" />
          <Bar dataKey="Income" fill={INCOME_COLOR} />
          <Bar dataKey="Expenses" fill={EXPENSE_COLOR} />
          <Bar dataKey="Net">
            {chartData.map((entry, i) => (
              <Cell key={i} fill={entry.Net >= 0 ? NET_POS_COLOR : NET_NEG_COLOR} fillOpacity={0.7} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

// ── Savings Rate ──────────────────────────────────────────────────────────────

export function SavingsRateReport({ options }) {
  const { showError } = useApp();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    api.reports.savingsRate(options.query)
      .then(res => setData(res?.data || []))
      .catch(showError)
      .finally(() => setLoading(false));
  }, [options.query, showError]);

  if (loading) return <Loading />;

  const chartData = data.map(d => ({
    month: d.month,
    'Savings Rate %': d.savingsRate,
  }));

  return (
    <div className="h-full w-full p-2">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={chartData} margin={{ top: 5, right: 20, left: 20, bottom: 60 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="month" angle={-45} textAnchor="end" interval={0} tick={{ fontSize: 10 }} />
          <YAxis tickFormatter={v => `${v.toFixed(0)}%`} tick={{ fontSize: 10 }} />
          <Tooltip formatter={(v) => `${v.toFixed(1)}%`} />
          <Legend />
          <ReferenceLine y={0} stroke="#666" />
          <Line type="monotone" dataKey="Savings Rate %" stroke={SAVINGS_COLOR} strokeWidth={2} dot={{ r: 3 }} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}

// ── Year-over-Year ────────────────────────────────────────────────────────────

export function YearOverYearReport({ options }) {
  const { showError } = useApp();
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

  if (loading) return <Loading />;

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
          Comparing <span className="font-semibold text-[#4e79a7]">{meta.currentPeriod}</span> vs <span className="font-semibold text-[#f28e2b]">{meta.previousPeriod}</span>
        </div>
      )}
      <div className="flex-1 min-h-0">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={chartData} margin={{ top: 5, right: 20, left: 20, bottom: 100 }}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="category" angle={-45} textAnchor="end" interval={0} tick={{ fontSize: 10 }} />
            <YAxis tickFormatter={currencyFormatter} tick={{ fontSize: 10 }} />
            <Tooltip formatter={(v) => `$${v.toFixed(2)}`} />
            <Legend />
            <Bar dataKey={currentKey} fill={CURRENT_COLOR} />
            <Bar dataKey={previousKey} fill={PREVIOUS_COLOR} />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
