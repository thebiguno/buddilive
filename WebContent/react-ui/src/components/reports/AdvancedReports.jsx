import { useState, useEffect } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  ResponsiveContainer, LineChart, Line, Cell
} from 'recharts';
import { api } from '../../lib/api';
import { useApp } from '../../context/AppContext';
import { Dialog, DialogContent, DialogFooter } from '../ui/Dialog';
import { Button } from '../ui/Button';
import { Select } from '../ui/Select';
import { Input } from '../ui/Input';

const COLORS = ['#4e79a7','#f28e2b','#e15759','#76b7b2','#59a14f','#edc948','#b07aa1','#ff9da7','#9c755f','#bab0ac'];

function Loading() {
  return <div className="flex items-center justify-center h-full text-sm text-gray-400">Loading...</div>;
}

function currencyFormatter(value) {
  if (Math.abs(value) >= 1000) return `$${(value / 1000).toFixed(1)}k`;
  return `$${value.toFixed(0)}`;
}

// ── Top Payees by Spend ───────────────────────────────────────────────────────

export function TopPayeesBySpendReport({ options }) {
  const { showError } = useApp();
  const [data, setData] = useState([]);
  const [total, setTotal] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    api.reports.topPayeesBySpend(options.query)
      .then(res => {
        setData(res?.data || []);
        setTotal(res?.totalSpendFormatted || '');
      })
      .catch(showError)
      .finally(() => setLoading(false));
  }, [options.query, showError]);

  if (loading) return <Loading />;

  const chartData = (data || []).map(d => ({
    payee: d.payee,
    Spend: d.spend,
    pct: d.percentFormatted,
    formatted: d.spendFormatted,
  }));

  return (
    <div className="h-full w-full p-2 flex flex-col gap-1">
      {total && (
        <div className="text-xs text-gray-500 text-center">Total spend: <span className="font-semibold">{total}</span></div>
      )}
      <div className="flex-1 min-h-0">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={chartData} layout="vertical" margin={{ top: 5, right: 80, left: 120, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis type="number" tickFormatter={currencyFormatter} tick={{ fontSize: 10 }} />
            <YAxis type="category" dataKey="payee" tick={{ fontSize: 10 }} width={115} />
            <Tooltip formatter={(v, _n, props) => [`${props.payload.formatted} (${props.payload.pct})`, 'Spend']} />
            <Bar dataKey="Spend" radius={[0, 3, 3, 0]}>
              {chartData.map((_, i) => (
                <Cell key={i} fill={COLORS[i % COLORS.length]} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

// ── Category Drill-Down ───────────────────────────────────────────────────────

export function CategoryDrillDownReport({ options }) {
  const { showError } = useApp();
  const [data, setData] = useState([]);
  const [categoryName, setCategoryName] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!options.categoryId) return;
    setLoading(true);
    api.reports.categoryDrillDown(options.categoryId, options.query)
      .then(res => {
        setData(res?.data || []);
        setCategoryName(res?.categoryName || '');
      })
      .catch(showError)
      .finally(() => setLoading(false));
  }, [options.categoryId, options.query, showError]);

  if (loading) return <Loading />;

  const chartData = (data || []).map(d => ({
    month: d.month,
    Amount: d.amount,
    formatted: d.amountFormatted,
  }));

  return (
    <div className="h-full w-full p-2 flex flex-col gap-1">
      {categoryName && (
        <div className="text-xs text-gray-500 text-center font-semibold">{categoryName}</div>
      )}
      <div className="flex-1 min-h-0">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={chartData} margin={{ top: 5, right: 20, left: 20, bottom: 60 }}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="month" angle={-45} textAnchor="end" interval={0} tick={{ fontSize: 10 }} />
            <YAxis tickFormatter={currencyFormatter} tick={{ fontSize: 10 }} />
            <Tooltip formatter={(v, _n, props) => [props.payload.formatted, 'Amount']} />
            <Legend />
            <Line type="monotone" dataKey="Amount" stroke={COLORS[0]} strokeWidth={2} dot={{ r: 3 }} />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

// ── Projected Balance ─────────────────────────────────────────────────────────

export function ProjectedBalanceReport({ options, accountTree }) {
  const { showError } = useApp();
  const [data, setData] = useState([]);
  const [series, setSeries] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const s = [];
    function walk(nodes) {
      (nodes || []).forEach(node => {
        if (node.nodeType === 'account') s.push({ key: `a${node.id}`, name: node.name });
        if (node.children) walk(node.children);
      });
    }
    walk(accountTree || []);
    setSeries(s);
  }, [accountTree]);

  useEffect(() => {
    setLoading(true);
    api.reports.projectedBalance(options.days || 90)
      .then(res => setData(res?.data || []))
      .catch(showError)
      .finally(() => setLoading(false));
  }, [options.days, showError]);

  if (loading) return <Loading />;

  return (
    <div className="h-full w-full p-2 flex flex-col gap-1">
      <div className="text-xs text-gray-500 text-center">Projected {options.days || 90} days forward using 12-month historical average</div>
      <div className="flex-1 min-h-0">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data} margin={{ top: 5, right: 20, left: 20, bottom: 60 }}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="date" angle={-90} textAnchor="end" interval="preserveStartEnd" tick={{ fontSize: 10 }} />
            <YAxis tickFormatter={currencyFormatter} tick={{ fontSize: 10 }} />
            <Tooltip formatter={(v) => `$${Number(v).toFixed(2)}`} />
            <Legend />
            <Line type="monotone" dataKey="netWorth" name="Net Worth" stroke="#4e79a7" strokeWidth={2} dot={false} strokeDasharray="5 5" />
            {series.map((s, i) => (
              <Line key={s.key} type="monotone" dataKey={s.key} name={s.name}
                stroke={COLORS[(i + 1) % COLORS.length]} dot={false} strokeWidth={1.5} />
            ))}
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

// ── Debt Paydown Tracker ──────────────────────────────────────────────────────

export function DebtPaydownReport({ options }) {
  const { showError } = useApp();
  const [data, setData] = useState([]);
  const [series, setSeries] = useState([]);
  const [noData, setNoData] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    api.reports.debtPaydown(options.query)
      .then(res => {
        if (res?.noData) { setNoData(true); return; }
        setData(res?.data || []);
        setSeries(res?.series || []);
      })
      .catch(showError)
      .finally(() => setLoading(false));
  }, [options.query, showError]);

  if (loading) return <Loading />;
  if (noData) return <div className="flex items-center justify-center h-full text-sm text-gray-400">No credit accounts found.</div>;

  return (
    <div className="h-full w-full p-2">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data} margin={{ top: 5, right: 20, left: 20, bottom: 60 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="date" angle={-90} textAnchor="end" interval="preserveStartEnd" tick={{ fontSize: 10 }} />
          <YAxis tickFormatter={currencyFormatter} tick={{ fontSize: 10 }} />
          <Tooltip formatter={(v) => `$${Number(v).toFixed(2)}`} />
          <Legend />
          {series.map((s, i) => (
            <Line key={s.id} type="monotone" dataKey={`a${s.id}`} name={s.name}
              stroke={COLORS[i % COLORS.length]} dot={false} strokeWidth={2} />
          ))}
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}

// ── Category Picker Dialog ────────────────────────────────────────────────────

const DRILL_INTERVALS = [
  { value: 'PLUGIN_FILTER_THIS_MONTH', text: 'This Month' },
  { value: 'PLUGIN_FILTER_LAST_MONTH', text: 'Last Month' },
  { value: 'PLUGIN_FILTER_THIS_QUARTER', text: 'This Quarter' },
  { value: 'PLUGIN_FILTER_LAST_QUARTER', text: 'Last Quarter' },
  { value: 'PLUGIN_FILTER_THIS_YEAR', text: 'This Year' },
  { value: 'PLUGIN_FILTER_THIS_YEAR_TO_DATE', text: 'This Year to Date' },
  { value: 'PLUGIN_FILTER_LAST_YEAR', text: 'Last Year' },
  { value: 'PLUGIN_FILTER_ALL_TIME', text: 'All Time' },
];

export function CategoryPickerDialog({ open, onClose, onConfirm }) {
  const { showError } = useApp();
  const [categories, setCategories] = useState([]);
  const [categoryId, setCategoryId] = useState('');
  const [interval, setInterval] = useState('PLUGIN_FILTER_THIS_YEAR');

  useEffect(() => {
    if (!open) return;
    api.categories.parents()
      .then(res => {
        const flat = (res?.data || []).filter(c => c.value !== '' && c.value != null);
        setCategories(flat);
        if (flat.length > 0) setCategoryId(String(flat[0].value));
      })
      .catch(showError);
  }, [open, showError]);

  function handleOk() {
    if (!categoryId) return;
    const selectedCat = categories.find(c => String(c.value) === categoryId);
    const dateRange = DRILL_INTERVALS.find(i => i.value === interval)?.text || interval;
    onConfirm({
      categoryId,
      categoryName: selectedCat?.text?.replace(/^[\s\u00a0]+/, '').trim() || categoryId,
      query: `interval=${interval}`,
      dateRange,
    });
    onClose();
  }

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent title="Category Drill-Down" className="w-[480px]" onOk={handleOk} onCancel={onClose}>
        <div className="p-3 flex flex-col gap-3">
          <div className="flex items-center gap-2">
            <label className="text-xs text-gray-700 w-24 flex-shrink-0">Category</label>
            <Select className="flex-1" value={categoryId} onChange={e => setCategoryId(e.target.value)}>
              {categories.map(c => (
                <option key={c.value} value={String(c.value)}>{c.text}</option>
              ))}
            </Select>
          </div>
          <div className="flex items-center gap-2">
            <label className="text-xs text-gray-700 w-24 flex-shrink-0">Interval</label>
            <Select className="flex-1" value={interval} onChange={e => setInterval(e.target.value)}>
              {DRILL_INTERVALS.map(i => (
                <option key={i.value} value={i.value}>{i.text}</option>
              ))}
            </Select>
          </div>
        </div>
        <DialogFooter>
          <Button variant="default" onClick={onClose}>Cancel</Button>
          <Button variant="primary" disabled={!categoryId} onClick={handleOk}>OK</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

// ── Projected Balance Options Dialog ─────────────────────────────────────────

const PROJECTION_OPTIONS = [
  { value: '30', label: '30 days' },
  { value: '60', label: '60 days' },
  { value: '90', label: '90 days' },
  { value: '180', label: '6 months' },
  { value: '365', label: '1 year' },
];

export function ProjectedBalancePickerDialog({ open, onClose, onConfirm }) {
  const [days, setDays] = useState('90');

  function handleOk() {
    onConfirm({ days });
    onClose();
  }

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent title="Projected Balance" className="w-80" onOk={handleOk} onCancel={onClose}>
        <div className="p-3 flex flex-col gap-2">
          <div className="flex items-center gap-2">
            <label className="text-xs text-gray-700 w-24 flex-shrink-0">Project forward</label>
            <Select className="flex-1" value={days} onChange={e => setDays(e.target.value)}>
              {PROJECTION_OPTIONS.map(o => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </Select>
          </div>
        </div>
        <DialogFooter>
          <Button variant="default" onClick={onClose}>Cancel</Button>
          <Button variant="primary" onClick={handleOk}>OK</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
