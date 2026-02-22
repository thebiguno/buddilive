import { useState, useEffect, useRef } from 'react';
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

function Loading({ t }) {
  return <div className="flex items-center justify-center h-full text-sm text-gray-400">{t('LOADING', 'Loading...')}</div>;
}

function currencyFormatter(value) {
  if (Math.abs(value) >= 1000) return `$${(value / 1000).toFixed(1)}k`;
  return `$${value.toFixed(0)}`;
}

// ── Top Payees by Spend ───────────────────────────────────────────────────────

export function TopPayeesBySpendReport({ options }) {
  const { showError, t } = useApp();
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

  if (loading) return <Loading t={t} />;

  const spendKey = t('SPEND', 'Spend');

  const chartData = (data || []).map(d => ({
    payee: d.payee,
    [spendKey]: d.spend,
    pct: d.percentFormatted,
    formatted: d.spendFormatted,
  }));

  return (
    <div className="h-full w-full p-2 flex flex-col gap-1">
      {total && (
        <div className="text-xs text-gray-500 text-center">{t('TOTAL_SPEND', 'Total spend')}: <span className="font-semibold">{total}</span></div>
      )}
      <div className="flex-1 min-h-0">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={chartData} layout="vertical" margin={{ top: 5, right: 80, left: 120, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis type="number" tickFormatter={currencyFormatter} tick={{ fontSize: 10 }} />
            <YAxis type="category" dataKey="payee" tick={{ fontSize: 10 }} width={115} />
            <Tooltip formatter={(v, _n, props) => [`${props.payload.formatted} (${props.payload.pct})`, spendKey]} />
            <Bar dataKey={spendKey} radius={[0, 3, 3, 0]}>
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

const ROOT_INCOME_ID = '__income__';
const ROOT_EXPENSES_ID = '__expenses__';

function normalizeCategoryText(text) {
  return String(text || '').replace(/^[\s\u00a0]+/, '').trim();
}

function getImmediateChildren(nodeId, categories) {
  const id = String(nodeId);
  if (id === ROOT_INCOME_ID) {
    return categories.filter(c => c.income === true && (c.parent == null || c.parent === ''));
  }
  if (id === ROOT_EXPENSES_ID) {
    return categories.filter(c => c.income === false && (c.parent == null || c.parent === ''));
  }
  return categories.filter(c => String(c.parent) === id);
}

function formatTooltipCurrency(value) {
  return `$${Number(value || 0).toFixed(2)}`;
}

export function CategoryDrillDownReport({ options }) {
  const { showError, t } = useApp();
  const [allCategories, setAllCategories] = useState([]);
  const [contexts, setContexts] = useState([]);
  const [loading, setLoading] = useState(false);
  const drillRequestIdRef = useRef(0);

  useEffect(() => {
    let cancelled = false;
    if (!options.categoryId) return undefined;

    async function loadInitialContext() {
      setLoading(true);
      try {
        const parents = await api.categories.parents();
        if (cancelled) return;

        const categories = (parents?.data || [])
          .filter(c => c.value !== '' && c.value != null)
          .map(c => ({ ...c, id: String(c.value), name: normalizeCategoryText(c.text) }));
        setAllCategories(categories);

        const rootId = String(options.categoryId);
        const rootName = options.categoryName || (
          rootId === ROOT_INCOME_ID ? t('INCOME', 'Income')
          : rootId === ROOT_EXPENSES_ID ? t('EXPENSES', 'Expenses')
          : (categories.find(c => String(c.value) === rootId)?.name || rootId)
        );

        const rootResponse = await api.reports.categoryDrillDown(rootId, options.query);
        if (cancelled) return;

        const key = `s_${rootId}`;
        const rootData = (rootResponse?.data || []).map(row => ({
          month: row.month,
          [key]: Number(row.totalAmount ?? (Number(row.amount || 0) + Number(row.childRollupAmount || 0))),
          [`${key}Formatted`]: row.totalAmountFormatted || row.amountFormatted,
        }));

        setContexts([{
          nodeId: rootId,
          title: rootName,
          series: [{ id: rootId, key, name: rootName }],
          data: rootData,
        }]);
      } catch (e) {
        if (!cancelled) showError(e);
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadInitialContext();
    return () => { cancelled = true; };
  }, [options.categoryId, options.categoryName, options.query, showError, t]);

  async function drillIntoSeries(seriesItem) {
    if (loading) return;
    const children = getImmediateChildren(seriesItem.id, allCategories);
    if (!children.length) return;

    const requestId = ++drillRequestIdRef.current;
    setLoading(true);
    try {
      const fetched = await Promise.all(children.map(async child => {
        const childId = String(child.value);
        const childName = normalizeCategoryText(child.text);
        const res = await api.reports.categoryDrillDown(childId, options.query);
        return { childId, childName, data: (res?.data || []) };
      }));
      if (requestId !== drillRequestIdRef.current) return;

      const monthMap = new Map();
      const nextSeries = [];

      fetched.forEach((entry) => {
        const key = `s_${entry.childId}`;
        nextSeries.push({ id: entry.childId, key, name: entry.childName });
        entry.data.forEach(row => {
          const existing = monthMap.get(row.month) || { month: row.month };
          existing[key] = Number(row.totalAmount ?? (Number(row.amount || 0) + Number(row.childRollupAmount || 0)));
          existing[`${key}Formatted`] = row.totalAmountFormatted || row.amountFormatted;
          monthMap.set(row.month, existing);
        });
      });

      const nextData = Array.from(monthMap.values()).sort((a, b) => String(a.month).localeCompare(String(b.month)));
      setContexts(prev => [...prev, {
        nodeId: seriesItem.id,
        title: seriesItem.name,
        series: nextSeries,
        data: nextData,
      }]);
    } catch (e) {
      if (requestId === drillRequestIdRef.current) showError(e);
    } finally {
      if (requestId === drillRequestIdRef.current) setLoading(false);
    }
  }

  function moveUp() {
    // Cancel any in-flight drill request and restore interaction immediately.
    drillRequestIdRef.current++;
    setLoading(false);
    setContexts(prev => (prev.length > 1 ? prev.slice(0, -1) : prev));
  }

  function isSeriesDrillable(series) {
    return getImmediateChildren(series.id, allCategories).length > 0;
  }

  const current = contexts[contexts.length - 1];
  if (loading && !current) return <Loading t={t} />;
  if (!current) return <div className="flex items-center justify-center h-full text-sm text-gray-400">{t('LOADING', 'Loading...')}</div>;

  const breadcrumbs = contexts.map(c => c.title).filter(Boolean).join(' / ');

  return (
    <div className="h-full w-full p-2 flex flex-col gap-1">
      <div className="flex items-center justify-between text-xs text-gray-500">
        <div className="font-semibold truncate pr-2">{breadcrumbs}</div>
        <div className="flex items-center gap-2">
          {loading && <span className="text-[11px] text-gray-400">{t('LOADING', 'Loading...')}</span>}
          <Button variant="default" className="h-6 px-2 py-0 text-[11px]" disabled={contexts.length <= 1} onClick={moveUp}>
            {t('PREVIOUS', 'Previous')}
          </Button>
        </div>
      </div>
      <div className="flex items-center gap-1 flex-wrap text-[11px] text-gray-600">
        <span>{t('DRILL_INTO', 'Drill into')}:</span>
        {current.series.map(series => {
          const drillable = isSeriesDrillable(series);
          return (
            <button
              key={series.key}
              type="button"
              disabled={!drillable}
              onClick={() => drillIntoSeries(series)}
              className={`px-2 py-0.5 rounded border ${drillable ? 'border-blue-300 text-blue-700 hover:bg-blue-50' : 'border-gray-200 text-gray-400 cursor-not-allowed'} disabled:opacity-60`}
              title={drillable ? t('CLICK_LINE_TO_DRILL', 'Click to drill down') : t('NO_CHILD_CATEGORIES', 'No child categories')}
            >
              {series.name}
            </button>
          );
        })}
      </div>
      <div className="flex-1 min-h-0">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart
            data={current.data}
            margin={{ top: 5, right: 20, left: 20, bottom: 60 }}
          >
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="month" angle={-45} textAnchor="end" interval={0} tick={{ fontSize: 10 }} />
            <YAxis tickFormatter={currencyFormatter} tick={{ fontSize: 10 }} />
            <Tooltip formatter={(value, name, props) => {
              const formatted = props?.payload?.[`${name}Formatted`];
              return [formatted || formatTooltipCurrency(value), name];
            }} />
            <Legend onClick={(entry) => {
              const target = current.series.find(series => series.key === entry?.dataKey || series.name === entry?.value);
              if (target && isSeriesDrillable(target) && !loading) drillIntoSeries(target);
            }} />
            {current.series.map((series, i) => {
              const drillable = isSeriesDrillable(series);
              return (
                <Line
                  key={series.key}
                  type="monotone"
                  dataKey={series.key}
                  name={series.name}
                  stroke={COLORS[i % COLORS.length]}
                  strokeWidth={2}
                  dot={{
                    r: 3,
                    style: { cursor: drillable ? 'pointer' : 'default' },
                    onClick: () => {
                      if (drillable && !loading) drillIntoSeries(series);
                    },
                  }}
                  style={{ cursor: drillable ? 'pointer' : 'default' }}
                  onClick={() => drillIntoSeries(series)}
                />
              );
            })}
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

// ── Projected Balance ─────────────────────────────────────────────────────────

export function ProjectedBalanceReport({ options, accountTree }) {
  const { showError, t } = useApp();
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

  if (loading) return <Loading t={t} />;

  return (
    <div className="h-full w-full p-2 flex flex-col gap-1">
      <div className="text-xs text-gray-500 text-center">{t('PROJECTED_DAYS_FORWARD', 'Projected {{days}} days forward using 12-month historical average').replace('{{days}}', String(options.days || 90))}</div>
      <div className="flex-1 min-h-0">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data} margin={{ top: 5, right: 20, left: 20, bottom: 60 }}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="date" angle={-90} textAnchor="end" interval="preserveStartEnd" tick={{ fontSize: 10 }} />
            <YAxis tickFormatter={currencyFormatter} tick={{ fontSize: 10 }} />
            <Tooltip formatter={(v) => `$${Number(v).toFixed(2)}`} />
            <Legend />
            <Line type="monotone" dataKey="netWorth" name={t('NET_WORTH', 'Net Worth')} stroke="#4e79a7" strokeWidth={2} dot={false} strokeDasharray="5 5" />
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
  const { showError, t } = useApp();
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

  if (loading) return <Loading t={t} />;
  if (noData) return <div className="flex items-center justify-center h-full text-sm text-gray-400">{t('NO_CREDIT_ACCOUNTS_FOUND', 'No credit accounts found.')}</div>;

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
  { value: 'PLUGIN_FILTER_THIS_MONTH', key: 'INTERVAL_THIS_MONTH', text: 'This Month' },
  { value: 'PLUGIN_FILTER_LAST_MONTH', key: 'INTERVAL_LAST_MONTH', text: 'Last Month' },
  { value: 'PLUGIN_FILTER_THIS_QUARTER', key: 'INTERVAL_THIS_QUARTER', text: 'This Quarter' },
  { value: 'PLUGIN_FILTER_LAST_QUARTER', key: 'INTERVAL_LAST_QUARTER', text: 'Last Quarter' },
  { value: 'PLUGIN_FILTER_THIS_YEAR', key: 'INTERVAL_THIS_YEAR', text: 'This Year' },
  { value: 'PLUGIN_FILTER_THIS_YEAR_TO_DATE', key: 'INTERVAL_THIS_YEAR_TO_DATE', text: 'This Year to Date' },
  { value: 'PLUGIN_FILTER_LAST_YEAR', key: 'INTERVAL_LAST_YEAR', text: 'Last Year' },
  { value: 'PLUGIN_FILTER_ALL_TIME', key: 'INTERVAL_ALL_TIME', text: 'All Time' },
];

export function CategoryPickerDialog({ open, onClose, onConfirm }) {
  const { showError, t } = useApp();
  const [categories, setCategories] = useState([]);
  const [categoryId, setCategoryId] = useState('');
  const [interval, setInterval] = useState('PLUGIN_FILTER_THIS_YEAR');

  useEffect(() => {
    if (!open) return;
    api.categories.parents()
      .then(res => {
        const flat = (res?.data || []).filter(c => c.value !== '' && c.value != null);
        const incomeCategories = flat
          .filter(c => c.income === true)
          .map(c => ({ ...c, text: `\u00a0\u00a0${c.text}` }));
        const expenseCategories = flat
          .filter(c => c.income === false)
          .map(c => ({ ...c, text: `\u00a0\u00a0${c.text}` }));
        const options = [
          { value: '__income__', text: t('INCOME', 'Income') },
          ...incomeCategories,
          { value: '__expenses__', text: t('EXPENSES', 'Expenses') },
          ...expenseCategories,
        ];
        setCategories(options);
        if (options.length > 0) setCategoryId(String(options[0].value));
      })
      .catch(showError);
  }, [open, showError, t]);

  function handleOk() {
    if (!categoryId) return;
    const selectedCat = categories.find(c => String(c.value) === categoryId);
    const selectedInterval = DRILL_INTERVALS.find(i => i.value === interval);
    const dateRange = selectedInterval ? t(selectedInterval.key, selectedInterval.text) : interval;
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
      <DialogContent title={t('REPORT_CATEGORY_DRILLDOWN', 'Category Drill-Down')} className="w-[480px]" onOk={handleOk} onCancel={onClose}>
        <div className="p-3 flex flex-col gap-3">
          <div className="flex items-center gap-2">
            <label className="text-xs text-gray-700 w-24 flex-shrink-0">{t('CATEGORY', 'Category')}</label>
            <Select className="flex-1" value={categoryId} onChange={e => setCategoryId(e.target.value)}>
              {categories.map(c => (
                <option key={c.value} value={String(c.value)}>{c.text}</option>
              ))}
            </Select>
          </div>
          <div className="flex items-center gap-2">
            <label className="text-xs text-gray-700 w-24 flex-shrink-0">{t('INTERVAL', 'Interval')}</label>
            <Select className="flex-1" value={interval} onChange={e => setInterval(e.target.value)}>
              {DRILL_INTERVALS.map(i => (
                <option key={i.value} value={i.value}>{t(i.key, i.text)}</option>
              ))}
            </Select>
          </div>
        </div>
        <DialogFooter>
          <Button variant="default" onClick={onClose}>{t('CANCEL', 'Cancel')}</Button>
          <Button variant="primary" disabled={!categoryId} onClick={handleOk}>{t('OK', 'OK')}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

// ── Projected Balance Options Dialog ─────────────────────────────────────────

const PROJECTION_OPTIONS = [
  { value: '30', key: 'PROJECTION_30_DAYS', label: '30 days' },
  { value: '60', key: 'PROJECTION_60_DAYS', label: '60 days' },
  { value: '90', key: 'PROJECTION_90_DAYS', label: '90 days' },
  { value: '180', key: 'PROJECTION_6_MONTHS', label: '6 months' },
  { value: '365', key: 'PROJECTION_1_YEAR', label: '1 year' },
];

export function ProjectedBalancePickerDialog({ open, onClose, onConfirm }) {
  const { t } = useApp();
  const [days, setDays] = useState('90');

  function handleOk() {
    onConfirm({ days });
    onClose();
  }

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent title={t('REPORT_PROJECTED_BALANCE', 'Projected Balance')} className="w-80" onOk={handleOk} onCancel={onClose}>
        <div className="p-3 flex flex-col gap-2">
          <div className="flex items-center gap-2">
            <label className="text-xs text-gray-700 w-24 flex-shrink-0">{t('PROJECT_FORWARD', 'Project forward')}</label>
            <Select className="flex-1" value={days} onChange={e => setDays(e.target.value)}>
              {PROJECTION_OPTIONS.map(o => (
                <option key={o.value} value={o.value}>{t(o.key, o.label)}</option>
              ))}
            </Select>
          </div>
        </div>
        <DialogFooter>
          <Button variant="default" onClick={onClose}>{t('CANCEL', 'Cancel')}</Button>
          <Button variant="primary" onClick={handleOk}>{t('OK', 'OK')}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
