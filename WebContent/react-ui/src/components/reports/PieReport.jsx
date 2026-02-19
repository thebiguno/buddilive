import { useState, useEffect } from 'react';
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { api } from '../../lib/api';
import { useApp } from '../../context/AppContext';

const COLORS = ['#4e79a7','#f28e2b','#e15759','#76b7b2','#59a14f','#edc948','#b07aa1','#ff9da7','#9c755f','#bab0ac'];

export function PieReport({ type, options }) {
  const { showError, t } = useApp();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    api.reports.pie(type, options.query)
      .then(res => setData(res?.data || []))
      .catch(e => { showError(e); setError(e.message); })
      .finally(() => setLoading(false));
  }, [type, options.query, showError]);

  if (loading) return <div className="flex items-center justify-center h-full text-sm text-gray-400">{t('LOADING', 'Loading...')}</div>;
  if (error) return <div className="flex items-center justify-center h-full text-sm text-red-500">{error}</div>;
  if (!data.length) return <div className="flex items-center justify-center h-full text-sm text-gray-400">{t('NO_DATA_FOR_PERIOD', 'No data for this period.')}</div>;

  return (
    <div className="flex flex-col h-full w-full p-4">
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie
            data={data}
            dataKey="amount"
            nameKey="label"
            cx="50%"
            cy="50%"
            outerRadius="60%"
            label={({ label, percent }) => `${label} (${(percent * 100).toFixed(1)}%)`}
            labelLine={true}
          >
            {data.map((_, i) => (
              <Cell key={i} fill={COLORS[i % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip formatter={(value, name, props) => [props.payload.formattedAmount || value, props.payload.label]} />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
