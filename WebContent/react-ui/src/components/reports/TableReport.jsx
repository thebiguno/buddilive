import { useState, useEffect } from 'react';
import { ChevronDown, ChevronRight } from 'lucide-react';
import { api } from '../../lib/api';
import { useApp } from '../../context/AppContext';
import { cn } from '../../lib/utils';

function parseStyle(styleStr) {
  if (!styleStr) return {};
  const result = {};
  (styleStr || '').split(';').forEach(part => {
    const [key, val] = part.split(':');
    if (key && val) {
      const camel = key.trim().replace(/-([a-z])/g, (_, c) => c.toUpperCase());
      result[camel] = val.trim();
    }
  });
  return result;
}

function ExpandableRow({ row, columns, isTotalRow }) {
  const [expanded, setExpanded] = useState(false);
  const hasTransactions = row.transactions && row.transactions.length > 0;

  return (
    <>
      <tr
        className={cn(
          'border-b border-gray-200 text-xs',
          isTotalRow ? 'font-bold bg-[#e8e8e8]' : 'hover:bg-blue-50 cursor-pointer'
        )}
        onClick={() => !isTotalRow && hasTransactions && setExpanded(v => !v)}
      >
        {columns.map((col, i) => (
          <td
            key={col.field}
            className={cn('px-2 py-1', col.align === 'right' ? 'text-right' : 'text-left')}
            style={parseStyle(row[col.field + 'Style'])}
          >
            {i === 0 && !isTotalRow && hasTransactions && (
              <span className="mr-1 inline-flex items-center">
                {expanded ? <ChevronDown size={10} /> : <ChevronRight size={10} />}
              </span>
            )}
            <span style={parseStyle(row[col.field + 'Style'])}>{row[col.field]}</span>
          </td>
        ))}
      </tr>
      {expanded && hasTransactions && (
        <tr className="bg-[#f8f8f8]">
          <td colSpan={columns.length} className="px-4 py-1">
            <table className="w-full text-xs">
              <thead>
                <tr className="font-semibold border-b border-gray-300">
                  <th className="text-left py-0.5 w-1/6">Date</th>
                  <th className="text-left py-0.5 w-1/4">Description</th>
                  <th className="text-left py-0.5 w-1/4">From → To</th>
                  <th className="text-right py-0.5 w-1/6">Amount</th>
                </tr>
              </thead>
              <tbody>
                {row.transactions.map((t, i) => (
                  <tr key={i} className={i % 2 === 0 ? 'bg-white' : 'bg-[#f5f5f5]'}>
                    <td className="py-0.5" style={parseStyle(t.dateStyle)}>{t.date}</td>
                    <td className="py-0.5" style={parseStyle(t.descriptionStyle)}>{t.description}</td>
                    <td className="py-0.5" style={parseStyle(t.fromToStyle)}>{t.from} → {t.to}</td>
                    <td className="text-right py-0.5" style={parseStyle(t.amountStyle)}>{t.amount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </td>
        </tr>
      )}
    </>
  );
}

export function IncomeExpensesReport({ options }) {
  const { showError } = useApp();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    api.reports.incomeExpenses(options.query)
      .then(res => setData(res?.data || []))
      .catch(showError)
      .finally(() => setLoading(false));
  }, [options.query, showError]);

  const columns = [
    { field: 'source', label: 'Category' },
    { field: 'actual', label: 'Actual', align: 'right' },
    { field: 'budgeted', label: 'Budgeted', align: 'right' },
    { field: 'difference', label: 'Difference', align: 'right' },
  ];

  return <ReportTable data={data} columns={columns} loading={loading} totalField="source" totalValue="Total" />;
}

export function AverageIncomeExpensesReport({ options }) {
  const { showError } = useApp();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    api.reports.averageIncomeExpenses(options.query)
      .then(res => setData(res?.data || []))
      .catch(showError)
      .finally(() => setLoading(false));
  }, [options.query, showError]);

  const columns = [
    { field: 'source', label: 'Category' },
    { field: 'average', label: 'Avg Actual', align: 'right' },
    { field: 'averageBudgeted', label: 'Avg Budgeted', align: 'right' },
    { field: 'difference', label: 'Difference', align: 'right' },
    { field: 'period', label: 'Period', align: 'right' },
  ];

  return <ReportTable data={data} columns={columns} loading={loading} totalField="source" totalValue="Total" />;
}

export function InflowByAccountReport({ options }) {
  const { showError } = useApp();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    api.reports.inflowByAccount(options.query)
      .then(res => setData(res?.data || []))
      .catch(showError)
      .finally(() => setLoading(false));
  }, [options.query, showError]);

  const columns = [
    { field: 'source', label: 'Account' },
    { field: 'inflow', label: 'Inflow', align: 'right' },
    { field: 'outflow', label: 'Outflow', align: 'right' },
    { field: 'difference', label: 'Difference', align: 'right' },
  ];

  return <ReportTable data={data} columns={columns} loading={loading} totalField="source" totalValue="Total" />;
}

export function InflowByPayeeReport({ options }) {
  const { showError } = useApp();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    api.reports.inflowByPayee(options.query)
      .then(res => setData(res?.data || []))
      .catch(showError)
      .finally(() => setLoading(false));
  }, [options.query, showError]);

  const columns = [
    { field: 'source', label: 'Payee' },
    { field: 'inflow', label: 'Inflow', align: 'right' },
    { field: 'outflow', label: 'Outflow', align: 'right' },
    { field: 'difference', label: 'Difference', align: 'right' },
  ];

  return <ReportTable data={data} columns={columns} loading={loading} totalField="source" totalValue="Total" />;
}

function ReportTable({ data, columns, loading, totalField, totalValue }) {
  if (loading) return <div className="flex items-center justify-center h-full text-sm text-gray-400">Loading...</div>;

  return (
    <div className="h-full overflow-auto">
      <table className="w-full border-collapse">
        <thead className="sticky top-0 bg-gradient-to-b from-[#d8d8d8] to-[#c8c8c8]">
          <tr>
            {columns.map(col => (
              <th
                key={col.field}
                className={cn('px-2 py-1 text-xs font-semibold border-b border-gray-300', col.align === 'right' ? 'text-right' : 'text-left')}
              >
                {col.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.map((row, i) => (
            <ExpandableRow
              key={i}
              row={row}
              columns={columns}
              isTotalRow={totalField && row[totalField] === totalValue}
            />
          ))}
        </tbody>
      </table>
    </div>
  );
}
