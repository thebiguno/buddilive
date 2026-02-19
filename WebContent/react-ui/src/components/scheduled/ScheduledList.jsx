import { useState, useEffect, useCallback } from 'react';
import { api } from '../../lib/api';
import { useApp } from '../../context/AppContext';
import { cn } from '../../lib/utils';
import { ContextMenu } from '../ui/ContextMenu';

const FREQUENCY_LABEL_KEYS = {
  SCHEDULE_FREQUENCY_MONTHLY_BY_DATE: 'SCHEDULE_FREQUENCY_MONTHLY_BY_DATE_LABEL',
  SCHEDULE_FREQUENCY_MONTHLY_BY_DAY_OF_WEEK: 'SCHEDULE_FREQUENCY_MONTHLY_BY_DAY_OF_WEEK_LABEL',
  SCHEDULE_FREQUENCY_WEEKLY: 'SCHEDULE_FREQUENCY_WEEKLY_LABEL',
  SCHEDULE_FREQUENCY_BIWEEKLY: 'SCHEDULE_FREQUENCY_BIWEEKLY_LABEL',
  SCHEDULE_FREQUENCY_EVERY_DAY: 'SCHEDULE_FREQUENCY_EVERY_DAY_LABEL',
  SCHEDULE_FREQUENCY_EVERY_X_DAYS: 'SCHEDULE_FREQUENCY_EVERY_X_DAYS_LABEL',
  SCHEDULE_FREQUENCY_EVERY_WEEKDAY: 'SCHEDULE_FREQUENCY_EVERY_WEEKDAY_LABEL',
  SCHEDULE_FREQUENCY_MULTIPLE_WEEKS_EVERY_MONTH: 'SCHEDULE_FREQUENCY_MULTIPLE_WEEKS_EVERY_MONTH_LABEL',
  SCHEDULE_FREQUENCY_MULTIPLE_MONTHS_EVERY_YEAR: 'SCHEDULE_FREQUENCY_MULTIPLE_MONTHS_EVERY_YEAR_LABEL',
};

const DAY_NAMES = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];
const MONTH_NAMES = ['January','February','March','April','May','June','July','August','September','October','November','December'];

function formatRepeat(row, t) {
  const base = t(FREQUENCY_LABEL_KEYS[row.repeat], row.repeat);
  const day = row.scheduleDay;
  switch (row.repeat) {
    case 'SCHEDULE_FREQUENCY_MONTHLY_BY_DATE': return `${base} (${t('DAY_LOWER', 'day')} ${day})`;
    case 'SCHEDULE_FREQUENCY_MONTHLY_BY_DAY_OF_WEEK': return `${base} (${t(`DAY_NAME_${day}`, DAY_NAMES[day] || day)})`;
    case 'SCHEDULE_FREQUENCY_WEEKLY': return `${base} (${t(`DAY_NAME_${day}`, DAY_NAMES[day] || day)})`;
    case 'SCHEDULE_FREQUENCY_BIWEEKLY': return `${base} (${t('EVERY_OTHER', 'every other')} ${t(`DAY_NAME_${day}`, DAY_NAMES[day] || day)})`;
    case 'SCHEDULE_FREQUENCY_EVERY_X_DAYS': return `${t('EVERY', 'Every')} ${day} ${t('DAYS_LOWER', 'days')}`;
    case 'SCHEDULE_FREQUENCY_MULTIPLE_MONTHS_EVERY_YEAR': {
      const months = [];
      const m = row.scheduleMonth || 0;
      MONTH_NAMES.forEach((name, i) => { if (m & (1 << i)) months.push(t(`MONTH_NAME_${i}`, name)); });
      return `${base} (${t('DAY_LOWER', 'day')} ${day}, ${months.join(', ')})`;
    }
    default: return base;
  }
}

export function ScheduledList({ selectedId, onSelect, onAdd, onEdit, onDelete }) {
  const { showError, t } = useApp();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [version, setVersion] = useState(0);
  const [ctxMenu, setCtxMenu] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await api.scheduled.list();
      setRows(data?.data || []);
    } catch (e) {
      showError(e);
    } finally {
      setLoading(false);
    }
  }, [showError]);

  useEffect(() => { load(); }, [load, version]);

  // Expose reload
  ScheduledList.reload = () => setVersion(v => v + 1);

  return (
    <div className="flex flex-col h-full">
      <div
        className="flex-1 overflow-auto"
        onContextMenu={e => { if (e.target === e.currentTarget || e.target.tagName === 'DIV') { e.preventDefault(); setCtxMenu({ x: e.clientX, y: e.clientY, row: null }); } }}
      >
        <table className="w-full border-collapse text-xs">
          <thead className="sticky top-0 bg-gradient-to-b from-[#d8d8d8] to-[#c8c8c8]">
            <tr>
              <th className="text-left px-2 py-1 border-b border-gray-300 font-semibold">{t('NAME', 'Name')}</th>
              <th className="text-left px-2 py-1 border-b border-gray-300 font-semibold">{t('REPEAT', 'Repeat')}</th>
              <th className="text-left px-2 py-1 border-b border-gray-300 font-semibold">{t('LAST_TRIGGERED', 'Last Triggered')}</th>
              <th className="text-left px-2 py-1 border-b border-gray-300 font-semibold">{t('END_DATE', 'End Date')}</th>
              <th className="text-right px-2 py-1 border-b border-gray-300 font-semibold">{t('AMOUNT', 'Amount')}</th>
              <th className="text-left px-2 py-1 border-b border-gray-300 font-semibold">{t('MESSAGE', 'Message')}</th>
            </tr>
          </thead>
          <tbody>
            {loading && (
              <tr><td colSpan={6} className="text-center py-4 text-gray-400">{t('LOADING', 'Loading...')}</td></tr>
            )}
            {rows.map((row, i) => (
              <tr
                key={row.id}
                className={cn(
                  'border-b border-gray-200 cursor-pointer hover:bg-blue-50',
                  i % 2 !== 0 ? 'bg-[#f5f5f5]' : 'bg-white',
                  selectedId === row.id ? '!bg-[#b8d0f0]' : ''
                )}
                onClick={() => onSelect && onSelect(row)}
                onContextMenu={e => { e.preventDefault(); onSelect && onSelect(row); setCtxMenu({ x: e.clientX, y: e.clientY, row }); }}
              >
                <td className="px-2 py-1">{row.name}</td>
                <td className="px-2 py-1">{formatRepeat(row, t)}</td>
                <td className="px-2 py-1">{row.lastCreatedDate}</td>
                <td className="px-2 py-1">{row.end}</td>
                <td className="px-2 py-1 text-right">
                  {(row.splits || []).map((s, j) => (
                    <div key={j}>{s.amount}</div>
                  ))}
                </td>
                <td className="px-2 py-1">{row.message}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {ctxMenu && (
        <ContextMenu
          x={ctxMenu.x}
          y={ctxMenu.y}
          onClose={() => setCtxMenu(null)}
          items={[
            { label: t('NEW_SCHEDULED_TRANSACTION', 'New Scheduled Transaction'), onClick: () => onAdd?.() },
            '-',
            { label: t('MODIFY_SCHEDULED_TRANSACTION', 'Edit Scheduled Transaction'), disabled: !ctxMenu.row, onClick: () => onEdit?.(ctxMenu.row) },
            { label: t('DELETE_SCHEDULED_TRANSACTION', 'Delete Scheduled Transaction'), disabled: !ctxMenu.row, onClick: () => onDelete?.(ctxMenu.row) },
          ]}
        />
      )}
    </div>
  );
}
