import { useState, useEffect, useCallback, useRef } from 'react';
import { api } from '../../lib/api';
import { useApp } from '../../context/AppContext';
import { Input } from '../ui/Input';
import { cn } from '../../lib/utils';
import { ContextMenu } from '../ui/ContextMenu';

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

function TransactionRow({ row, index, isSelected, onClick, showTimestamps }) {
  const splits = row.splits || [];
  const lastSplit = splits[splits.length - 1];
  const bg = isSelected ? 'bg-[#b8d0f0]' : (index % 2 !== 0 ? 'bg-[#f5f5f5]' : 'bg-white');

  return (
    <div
      className={cn('border-b border-gray-200 cursor-pointer hover:bg-blue-50 select-none', bg)}
      onClick={() => onClick(row)}
    >
      {/* Header row: date | bold description | (spans amount cols) | balance | [timestamps] */}
      <div className="flex items-center text-xs py-0.5 px-1">
        <span className="w-[13%] flex-shrink-0 truncate text-gray-700">{row.date}</span>
        <span className="flex-1 truncate font-bold">{row.description}</span>
        <span className="w-[12%] flex-shrink-0" />
        <span className="w-[12%] flex-shrink-0" />
        <span className="w-[12%] flex-shrink-0 text-right pr-1">
          {lastSplit && <span style={parseStyle(lastSplit.balanceStyle)}>{lastSplit.balance}</span>}
        </span>
        {showTimestamps && (
          <>
            <span className="w-[11%] flex-shrink-0 truncate text-gray-500 text-right pr-1">{row.created}</span>
            <span className="w-[11%] flex-shrink-0 truncate text-gray-500 text-right pr-1">{row.modified}</span>
          </>
        )}
      </div>
      {/* Split sub-rows: keep date col empty; show From → To under description | amount cols | [timestamp spacers] */}
      {splits.map((s, i) => (
        <div key={i} className="flex items-center text-xs py-0.5 px-1 text-gray-500 italic">
          <span className="w-[13%] flex-shrink-0" />
          <span className="flex-1 truncate">{s.from} → {s.to}</span>
          <span className="w-[12%] flex-shrink-0 text-right pr-1">
            {s.amountInDebitColumn && <span style={parseStyle(s.amountStyle)}>{s.amount}</span>}
          </span>
          <span className="w-[12%] flex-shrink-0 text-right pr-1">
            {!s.amountInDebitColumn && <span style={parseStyle(s.amountStyle)}>{s.amount}</span>}
          </span>
          <span className="w-[12%] flex-shrink-0" />
          {showTimestamps && (
            <>
              <span className="w-[11%] flex-shrink-0" />
              <span className="w-[11%] flex-shrink-0" />
            </>
          )}
        </div>
      ))}
    </div>
  );
}

export function TransactionList({ selectedAccount, onTransactionSelect, selectedTransactionId, onLoaded }) {
  const { transactionListVersion, showError, t } = useApp();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState('');
  const [showTimestamps, setShowTimestamps] = useState(false);
  const [sortBy, setSortBy] = useState('date');
  const [headerCtxMenu, setHeaderCtxMenu] = useState(null);
  const searchTimer = useRef(null);

  const load = useCallback(async () => {
    if (!selectedAccount) {
      setRows([]);
      onLoaded && onLoaded();
      return;
    }
    setLoading(true);
    try {
      const sortParam = sortBy === 'date' ? '' : `&sortBy=${sortBy}`;
      const params = `?source=${selectedAccount.id}&start=0&limit=500${search ? `&search=${encodeURIComponent(search)}` : ''}${sortParam}`;
      const data = await api.transactions.list(params);
      setRows(data?.data || []);
    } catch (e) {
      showError(e);
    } finally {
      setLoading(false);
      onLoaded && onLoaded();
    }
  }, [selectedAccount, search, showError, transactionListVersion, sortBy, onLoaded]);

  useEffect(() => { load(); }, [load]);

  function handleSearchChange(e) {
    const val = e.target.value;
    clearTimeout(searchTimer.current);
    searchTimer.current = setTimeout(() => setSearch(val), 300);
  }

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <div
        className="flex items-center text-xs font-semibold bg-gradient-to-b from-[#d8d8d8] to-[#c8c8c8] border-b border-gray-300 flex-shrink-0 px-1 py-1 select-none"
        onContextMenu={e => { e.preventDefault(); setHeaderCtxMenu({ x: e.clientX, y: e.clientY }); }}
      >
        <span className="w-[13%]">{t('DATE', 'Date')}</span>
        <span className="flex-1">{t('PAYEE_DESCRIPTION', 'Payee / Description')}</span>
        <span className="w-[12%] text-right pr-1">{t('AMOUNT_FROM', 'Amount From')}</span>
        <span className="w-[12%] text-right pr-1">{t('AMOUNT_TO', 'Amount To')}</span>
        <span className="w-[12%] text-right pr-1">{t('BALANCE', 'Balance')}</span>
        {showTimestamps && (
          <>
            <span className="w-[11%] text-right pr-1">{t('CREATED', 'Created')}</span>
            <span className="w-[11%] text-right pr-1">{t('MODIFIED', 'Modified')}</span>
          </>
        )}
      </div>
      {headerCtxMenu && (
        <ContextMenu
          x={headerCtxMenu.x}
          y={headerCtxMenu.y}
          onClose={() => setHeaderCtxMenu(null)}
          items={[
            {
              label: showTimestamps ? t('HIDE_AUDIT_TIMESTAMPS', 'Hide Audit Timestamps') : t('SHOW_AUDIT_TIMESTAMPS', 'Show Audit Timestamps'),
              onClick: () => {
                setShowTimestamps(v => {
                  if (v) setSortBy('date');
                  return !v;
                });
              },
            },
            ...(showTimestamps ? [
              { label: t('SORT_BY_TRANSACTION_DATE', 'Sort by Transaction Date'), onClick: () => setSortBy('date') },
              { label: t('SORT_BY_CREATED_DATE', 'Sort by Created Date'), onClick: () => setSortBy('created') },
              { label: t('SORT_BY_MODIFIED_DATE', 'Sort by Modified Date'), onClick: () => setSortBy('modified') },
            ] : []),
          ]}
        />
      )}
      {/* Rows */}
      <div className="flex-1 overflow-y-auto">
        {loading && <div className="p-2 text-xs text-gray-400">{t('LOADING', 'Loading...')}</div>}
        {!loading && !selectedAccount && (
          <div className="p-4 text-xs text-gray-400 text-center">{t('SELECT_ACCOUNT_TO_VIEW_TRANSACTIONS', 'Select an account to view transactions.')}</div>
        )}
        {rows.map((row, i) => (
          <TransactionRow
            key={row.id}
            row={row}
            index={i}
            isSelected={row.id === selectedTransactionId}
            onClick={onTransactionSelect}
            showTimestamps={showTimestamps}
          />
        ))}
      </div>
      {/* Bottom search bar */}
      <div className="flex items-center justify-end gap-2 px-2 py-1 bg-[#e8e8e8] border-t border-gray-300 flex-shrink-0">
        <Input
          className="w-48"
          defaultValue=""
          onChange={handleSearchChange}
          placeholder={t('SEARCH', 'Search...')}
        />
      </div>
    </div>
  );
}
