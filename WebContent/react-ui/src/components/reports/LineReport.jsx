import { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { api } from '../../lib/api';
import { useApp } from '../../context/AppContext';

const COLORS = ['#4e79a7','#f28e2b','#e15759','#76b7b2','#59a14f','#edc948','#b07aa1','#ff9da7','#9c755f','#bab0ac'];

export function BalancesOverTimeReport({ options, accountTree }) {
  const { showError } = useApp();
  const [data, setData] = useState([]);
  const [series, setSeries] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // Build series from account tree
    const s = [];
    function walk(nodes) {
      (nodes || []).forEach(node => {
        if (node.nodeType === 'account') {
          s.push({ key: `a${node.id}`, name: node.name });
        }
        if (node.children) walk(node.children);
      });
    }
    walk(accountTree || []);
    setSeries(s);
  }, [accountTree]);

  useEffect(() => {
    setLoading(true);
    api.reports.balancesOverTime(options.query)
      .then(res => setData(res?.data || []))
      .catch(showError)
      .finally(() => setLoading(false));
  }, [options.query, showError]);

  if (loading) return <div className="flex items-center justify-center h-full text-sm text-gray-400">Loading...</div>;

  return (
    <div className="h-full w-full p-2">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data} margin={{ top: 5, right: 20, left: 20, bottom: 60 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="date" angle={-90} textAnchor="end" interval="preserveStartEnd" tick={{ fontSize: 10 }} />
          <YAxis tick={{ fontSize: 10 }} />
          <Tooltip />
          <Legend />
          {series.map((s, i) => (
            <Line
              key={s.key}
              type="monotone"
              dataKey={s.key}
              name={s.name}
              stroke={COLORS[i % COLORS.length]}
              dot={false}
              strokeWidth={2}
            />
          ))}
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}

export function NetWorthOverTimeReport({ options }) {
  const { showError } = useApp();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    api.reports.netWorth(options.query)
      .then(res => setData(res?.data || []))
      .catch(showError)
      .finally(() => setLoading(false));
  }, [options.query, showError]);

  if (loading) return <div className="flex items-center justify-center h-full text-sm text-gray-400">Loading...</div>;

  return (
    <div className="h-full w-full p-2">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data} margin={{ top: 5, right: 20, left: 20, bottom: 60 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="date" angle={-90} textAnchor="end" interval="preserveStartEnd" tick={{ fontSize: 10 }} />
          <YAxis tick={{ fontSize: 10 }} />
          <Tooltip />
          <Legend />
          <Line type="monotone" dataKey="netWorth" name="Net Worth" stroke={COLORS[0]} dot={false} strokeWidth={2} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
