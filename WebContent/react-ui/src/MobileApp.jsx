import { useState, useEffect, useCallback, useRef } from 'react';
import { AppProvider, useApp } from './context/AppContext';
import { api } from './lib/api';
import { cn } from './lib/utils';
import { ConfirmDialog, AlertDialog } from './components/ui/ConfirmDialog';
import { ChevronDown, ChevronRight, ChevronLeft, Plus, ArrowRight, Minus, Trash2, RefreshCw, LogOut } from 'lucide-react';

function today() { return new Date().toISOString().split('T')[0]; }

function parseStyle(s) {
  if (!s) return {};
  const r = {};
  s.split(';').forEach(p => {
    const [k, v] = p.split(':');
    if (k && v) r[k.trim().replace(/-([a-z])/g, (_, c) => c.toUpperCase())] = v.trim();
  });
  return r;
}

// ── Account tree node ────────────────────────────────────────────────────────
function AccountNode({ node, depth, selectedId, expandedSet, onToggle, onSelect }) {
  const isAccount = node.nodeType === 'account';
  const hasChildren = node.children?.length > 0;
  const expanded = expandedSet.has(node.id);
  return (
    <div>
      <div
        className={cn('flex items-center py-3 border-b border-gray-100', isAccount ? 'cursor-pointer active:bg-blue-50' : '', node.id === selectedId ? 'bg-blue-100' : depth % 2 === 0 ? 'bg-white' : 'bg-gray-50')}
        style={{ paddingLeft: `${depth * 20 + 16}px`, paddingRight: 16 }}
        onClick={() => isAccount && onSelect(node)}
      >
        {hasChildren
          ? <button className="mr-2 text-gray-400 p-1 -ml-1" onClick={e => { e.stopPropagation(); onToggle(node.id); }}>{expanded ? <ChevronDown size={16} /> : <ChevronRight size={16} />}</button>
          : <span className="mr-2 w-5 flex-shrink-0" />}
        <span className="flex-1 text-sm truncate" style={parseStyle(node.style)}>{node.name}</span>
        {node.balance != null && <span className="ml-3 text-sm font-medium flex-shrink-0" style={parseStyle(node.balanceStyle)}>{node.balance}</span>}
        {isAccount && <ChevronRight size={14} className="ml-2 text-gray-300 flex-shrink-0" />}
      </div>
      {expanded && hasChildren && node.children.map(c => (
        <AccountNode key={c.id} node={c} depth={depth + 1} selectedId={selectedId} expandedSet={expandedSet} onToggle={onToggle} onSelect={onSelect} />
      ))}
    </div>
  );
}

// ── Account sidebar ──────────────────────────────────────────────────────────
function AccountSidebar({ selectedAccount, onSelect, visible }) {
  const { accountTreeVersion, showError, t } = useApp();
  const [nodes, setNodes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [expandedSet, setExpandedSet] = useState(new Set());

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await api.accounts.list();
      const children = data.children || [];
      setNodes(children);
      const ids = new Set();
      const collect = list => list.forEach(n => { ids.add(n.id); if (n.children) collect(n.children); });
      collect(children);
      setExpandedSet(ids);
    } catch (e) { showError(e); } finally { setLoading(false); }
  }, [showError]);

  useEffect(() => { load(); }, [load, accountTreeVersion]);

  return (
    <div className={cn('absolute inset-0 bg-white z-20 flex flex-col transition-transform duration-300', visible ? 'translate-x-0' : '-translate-x-full')}>
      <div className="flex items-center px-4 py-3 bg-gray-800 text-white flex-shrink-0">
        <button onClick={() => { window.location.href = 'authentication/logout'; }} className="p-1 mr-3 text-gray-300"><LogOut size={18} /></button>
        <span className="flex-1 text-base font-semibold">{t('MY_ACCOUNTS', 'Accounts')}</span>
        <button onClick={load} className="p-1 text-gray-300"><RefreshCw size={18} /></button>
      </div>
      <div className="flex-1 overflow-y-auto">
        {loading && <div className="p-4 text-sm text-gray-400 text-center">{t('LOADING', 'Loading...')}</div>}
        {nodes.map(n => <AccountNode key={n.id} node={n} depth={0} selectedId={selectedAccount?.id} expandedSet={expandedSet} onToggle={id => setExpandedSet(prev => { const s = new Set(prev); s.has(id) ? s.delete(id) : s.add(id); return s; })} onSelect={onSelect} />)}
      </div>
    </div>
  );
}

// ── Transaction row ──────────────────────────────────────────────────────────
function TxRow({ row, onClick }) {
  const splits = row.splits || [];
  const last = splits[splits.length - 1];
  return (
    <div className="px-4 py-3 border-b border-gray-100 bg-white active:bg-blue-50 cursor-pointer" onClick={() => onClick(row)}>
      <div className="flex items-start justify-between gap-2">
        <div className="flex-1 min-w-0">
          <div className="text-sm font-medium truncate">{row.description}</div>
          <div className="text-xs text-gray-400 mt-0.5">{row.date}</div>
          {splits.map((s, i) => <div key={i} className="text-xs text-gray-500 mt-0.5 truncate">{s.from} → {s.to}{s.memo ? ` · ${s.memo}` : ''}</div>)}
        </div>
        <div className="flex-shrink-0 text-right">
          {splits.map((s, i) => <div key={i} className="text-sm font-medium" style={parseStyle(s.amountStyle)}>{s.amount}</div>)}
          {last?.balance && <div className="text-xs text-gray-400 mt-0.5">{last.balance}</div>}
        </div>
      </div>
    </div>
  );
}

// ── Transaction list view ────────────────────────────────────────────────────
function TxListView({ account, onBack, onNew, onSelect }) {
  const { showError, transactionListVersion, t } = useApp();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState('');
  const timer = useRef(null);

  const load = useCallback(async () => {
    if (!account) return;
    setLoading(true);
    try {
      const p = `?source=${account.id}&start=0&limit=200${search ? `&search=${encodeURIComponent(search)}` : ''}`;
      const data = await api.transactions.list(p);
      setRows(data?.data || []);
    } catch (e) { showError(e); } finally { setLoading(false); }
  }, [account, search, showError, transactionListVersion]);

  useEffect(() => { load(); }, [load]);

  return (
    <div className="absolute inset-0 flex flex-col bg-white">
      <div className="flex items-center px-4 py-3 bg-gray-800 text-white flex-shrink-0">
        <button onClick={onBack} className="mr-3 text-gray-300"><ChevronLeft size={22} /></button>
        <div className="flex-1 min-w-0">
          <div className="text-base font-semibold truncate">{account?.name}</div>
          {account?.balance != null && <div className="text-xs text-gray-300">{account.balance}</div>}
        </div>
        <button onClick={onNew} className="ml-3 w-9 h-9 rounded-full bg-blue-500 flex items-center justify-center"><Plus size={20} /></button>
      </div>
      <div className="px-4 py-2 bg-gray-50 border-b border-gray-200 flex-shrink-0">
        <input type="search" className="w-full border border-gray-300 rounded-full px-4 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500" placeholder={t('SEARCH', 'Search...')} onChange={e => { clearTimeout(timer.current); timer.current = setTimeout(() => setSearch(e.target.value), 300); }} />
      </div>
      <div className="flex-1 overflow-y-auto">
        {loading && <div className="p-6 text-sm text-gray-400 text-center">{t('LOADING', 'Loading...')}</div>}
        {!loading && rows.length === 0 && <div className="p-6 text-sm text-gray-400 text-center">{t('NO_TRANSACTIONS_FOUND', 'No transactions found.')}</div>}
        {rows.map(r => <TxRow key={r.id} row={r} onClick={onSelect} />)}
      </div>
    </div>
  );
}

// ── Mobile select (native for mobile UX) ─────────────────────────────────────
function MSelect({ options, value, onChange, placeholder }) {
  return (
    <select className={cn('w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500', !value ? 'text-gray-400' : 'text-gray-900')} value={value || ''} onChange={e => onChange(e.target.value || null)}>
      <option value="">{placeholder}</option>
      {options.map(o => <option key={o.value} value={o.value}>{o.text}</option>)}
    </select>
  );
}

// ── Split row ────────────────────────────────────────────────────────────────
function SplitRow({ split, index, isOnly, splitSources, onUpdate, onAdd, onRemove }) {
  const { t } = useApp();
  const { from: fromOpts, to: toOpts } = splitSources;
  const upd = (f, v) => onUpdate(index, { ...split, [f]: v });
  const handleFrom = val => {
    const opt = fromOpts.find(o => String(o.value) === String(val));
    const u = { ...split, fromId: val ? Number(val) : null, fromType: opt?.type };
    if (split.source && val && Number(val) !== split.source) u.toId = split.source;
    if (Number(val) === u.toId) u.toId = null;
    onUpdate(index, u);
  };
  const handleTo = val => {
    const opt = toOpts.find(o => String(o.value) === String(val));
    const u = { ...split, toId: val ? Number(val) : null, toType: opt?.type };
    if (split.source && val && Number(val) !== split.source) u.fromId = split.source;
    if (Number(val) === u.fromId) u.fromId = null;
    onUpdate(index, u);
  };
  return (
    <div className="bg-gray-50 rounded-xl p-3 mb-2 border border-gray-200">
      <div className="flex items-center gap-2 mb-2">
        <input type="text" inputMode="decimal" className="flex-1 border border-gray-300 rounded-lg px-3 py-2.5 text-sm text-right focus:outline-none focus:ring-2 focus:ring-blue-500" value={split.amount || ''} onChange={e => upd('amount', e.target.value)} onBlur={e => { const n = parseFloat(e.target.value); if (!isNaN(n)) upd('amount', n.toFixed(2)); }} placeholder={t('AMOUNT_PLACEHOLDER', '0.00')} />
        <button className={cn('w-8 h-8 rounded-full flex items-center justify-center text-white', isOnly ? 'bg-gray-200' : 'bg-red-400')} onClick={() => !isOnly && onRemove(index)} disabled={isOnly}><Minus size={14} /></button>
        <button className="w-8 h-8 rounded-full bg-green-500 flex items-center justify-center text-white" onClick={() => onAdd(index)}><Plus size={14} /></button>
      </div>
      <div className="flex items-center gap-2 mb-2">
        <div className="flex-1 min-w-0"><MSelect options={fromOpts} value={split.fromId} onChange={handleFrom} placeholder={t('FROM', 'From')} /></div>
        <ArrowRight size={16} className="text-gray-400 flex-shrink-0" />
        <div className="flex-1 min-w-0"><MSelect options={toOpts} value={split.toId} onChange={handleTo} placeholder={t('TO', 'To')} /></div>
      </div>
      <input type="text" className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" value={split.memo || ''} onChange={e => upd('memo', e.target.value)} placeholder={t('MEMO_OPTIONAL', 'Memo (optional)')} />
    </div>
  );
}

// ── Transaction form ─────────────────────────────────────────────────────────
const EMPTY_SPLIT = () => ({ amount: '', fromId: null, toId: null, memo: '' });

function TxFormView({ account, transaction, onSaved, onCancel, onDelete }) {
  const { splitSources, setSplitSources, showError, descriptionStoreVersion, t } = useApp();
  const [date, setDate] = useState(today());
  const [description, setDescription] = useState('');
  const [number, setNumber] = useState('');
  const [splits, setSplits] = useState([EMPTY_SPLIT()]);
  const [txId, setTxId] = useState(null);
  const [descs, setDescs] = useState([]);
  const [saving, setSaving] = useState(false);
  const [confirm, setConfirm] = useState(null);
  const [showSugg, setShowSugg] = useState(false);
  const loaded = useRef(null);

  useEffect(() => { if (splitSources.from.length === 0) api.transactions.splitSources().then(setSplitSources).catch(showError); }, [splitSources.from.length, setSplitSources, showError]);
  useEffect(() => { api.transactions.descriptions().then(d => setDescs((d?.data || []).map(x => ({ value: x.value, text: x.value, transaction: x.transaction })))).catch(() => {}); }, [descriptionStoreVersion]);

  useEffect(() => {
    if (transaction) {
      const id = transaction.id || null;
      const d = transaction.dateIso || today();
      const desc = transaction.description || '';
      const num = transaction.number || '';
      const s = (transaction.splits || []).map(sp => ({ amount: sp.amountNumber || sp.amount || '', fromId: sp.fromId || null, toId: sp.toId || null, memo: sp.memo || '', source: account?.id }));
      const ms = s.length > 0 ? s : [{ ...EMPTY_SPLIT(), source: account?.id }];
      setTxId(id); setDate(d); setDescription(desc); setNumber(num); setSplits(ms);
      loaded.current = { id, date: d, description: desc, number: num, splitsCount: ms.length };
    } else {
      setTxId(null); setDate(today()); setDescription(''); setNumber(''); setSplits([{ ...EMPTY_SPLIT(), source: account?.id }]);
      loaded.current = null;
    }
  }, [transaction, account]);

  useEffect(() => { setSplits(prev => prev.map(s => ({ ...s, source: account?.id }))); }, [account]);

  const updSplit = (i, u) => setSplits(prev => prev.map((s, j) => j === i ? u : s));
  const addSplit = i => setSplits(prev => [...prev.slice(0, i + 1), { ...EMPTY_SPLIT(), source: account?.id }, ...prev.slice(i + 1)]);
  const remSplit = i => setSplits(prev => prev.filter((_, j) => j !== i));

  const valid = () => {
    if (!date || !description.trim()) return false;
    return splits.every(s => s.amount && parseFloat(s.amount) !== 0 && s.fromId && s.toId);
  };

  async function doSave() {
    setSaving(true);
    try {
      await api.transactions.save({ action: txId ? 'update' : 'insert', ...(txId ? { id: txId } : {}), date, description: description.trim(), number, splits: splits.map(s => ({ amount: parseFloat(s.amount), fromId: s.fromId, toId: s.toId, memo: s.memo || '' })) });
      onSaved && onSaved();
    } catch (e) { showError(e); } finally { setSaving(false); }
  }

  function handleSave() {
    if (!valid()) return;
    const d = new Date(date + 'T00:00:00'), now = new Date();
    const future = new Date(now); future.setDate(future.getDate() + 7);
    const past = new Date(now); past.setMonth(past.getMonth() - 3);
    const oor = d > future || d < past;
    const l = loaded.current;
    const changed = !!txId && l && (l.date !== date || l.description !== description.trim() || l.number !== (number || '') || l.splitsCount !== splits.length);
    if (oor) {
      setConfirm({ title: t('DATE_OUT_OF_RANGE', 'Date Out of Range'), message: d > future ? t('TRANSACTION_DATE_TOO_FAR_FUTURE_MOBILE', 'Date is more than 7 days in the future. Save anyway?') : t('TRANSACTION_DATE_TOO_FAR_PAST_MOBILE', 'Date is more than 3 months in the past. Save anyway?'), onConfirm: () => { setConfirm(null); changed ? setConfirm({ title: t('MODIFY_TRANSACTION', 'Modify Transaction'), message: t('SAVE_CHANGES_CONFIRM', 'Save changes?'), onConfirm: () => { setConfirm(null); doSave(); }, onCancel: () => setConfirm(null) }) : doSave(); }, onCancel: () => setConfirm(null) });
    } else if (changed) {
      setConfirm({ title: t('MODIFY_TRANSACTION', 'Modify Transaction'), message: t('SAVE_CHANGES_TO_TRANSACTION_CONFIRM', 'Save changes to this transaction?'), onConfirm: () => { setConfirm(null); doSave(); }, onCancel: () => setConfirm(null) });
    } else { doSave(); }
  }

  const filtered = description.length > 0 ? descs.filter(d => d.text.toLowerCase().includes(description.toLowerCase()) && d.text !== description).slice(0, 6) : [];

  return (
    <div className="absolute inset-0 flex flex-col bg-gray-100">
      {confirm && <ConfirmDialog open title={confirm.title} message={confirm.message} onConfirm={confirm.onConfirm} onCancel={confirm.onCancel} />}
      <div className="flex items-center px-4 py-3 bg-gray-800 text-white flex-shrink-0">
        <button onClick={onCancel} className="mr-3 text-gray-300"><ChevronLeft size={22} /></button>
        <span className="flex-1 text-base font-semibold">{txId ? t('MODIFY_TRANSACTION', 'Edit Transaction') : t('NEW_TRANSACTION', 'New Transaction')}</span>
        {txId && <button onClick={() => onDelete && onDelete(txId)} className="mr-4 text-red-400"><Trash2 size={18} /></button>}
        <button onClick={handleSave} disabled={!valid() || saving} className={cn('text-sm font-semibold', valid() && !saving ? 'text-blue-300' : 'text-gray-500')}>{saving ? t('SAVING', 'Saving...') : (txId ? t('UPDATE', 'Update') : t('SAVE', 'Save'))}</button>
      </div>
      <div className="flex-1 overflow-y-auto">
        <div className="px-4 pt-4 pb-6">
          <div className="flex gap-3 mb-3">
            <div className="flex-1">
              <label className="block text-xs font-medium text-gray-500 mb-1 uppercase tracking-wide">{t('DATE', 'Date')}</label>
              <input type="date" className="w-full border border-gray-300 rounded-xl px-3 py-2.5 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500" value={date} onChange={e => setDate(e.target.value)} />
            </div>
            <div className="w-28">
              <label className="block text-xs font-medium text-gray-500 mb-1 uppercase tracking-wide">{t('NUMBER', 'Number')}</label>
              <input type="text" className="w-full border border-gray-300 rounded-xl px-3 py-2.5 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500" value={number} onChange={e => setNumber(e.target.value)} placeholder={t('OPTIONAL', 'Optional')} />
            </div>
          </div>
          <div className="mb-3 relative">
            <label className="block text-xs font-medium text-gray-500 mb-1 uppercase tracking-wide">{t('DESCRIPTION', 'Description')}</label>
            <input type="text" className="w-full border border-gray-300 rounded-xl px-3 py-2.5 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500" value={description} onChange={e => { setDescription(e.target.value); setShowSugg(true); }} onFocus={() => setShowSugg(true)} onBlur={() => setTimeout(() => setShowSugg(false), 150)} placeholder={t('PAYEE_DESCRIPTION', 'Payee / Description')} autoComplete="off" />
            {showSugg && filtered.length > 0 && (
              <div className="absolute left-0 right-0 top-full mt-1 bg-white border border-gray-200 rounded-xl shadow-lg z-30 overflow-hidden">
                {filtered.map(d => (
                  <button key={d.value} className="w-full text-left px-4 py-3 text-sm border-b border-gray-100 last:border-0 active:bg-blue-50" onMouseDown={() => {
                    setDescription(d.value); setShowSugg(false);
                    if (d.transaction) {
                      const currentAccountId = account?.id || null;
                      const isAcct = t => t === 'C' || t === 'D';
                      const s = (d.transaction.splits || []).map((sp, i) => {
                        let fromId = sp.fromId || null;
                        let toId = sp.toId || null;
                        if (isAcct(sp.fromType) && isAcct(sp.toType)) { fromId = currentAccountId; }
                        else if (isAcct(sp.fromType)) { fromId = currentAccountId; }
                        else if (isAcct(sp.toType)) { toId = currentAccountId; }
                        return { amount: splits[i]?.amount || sp.amountNumber || sp.amount || '', fromId, toId, memo: splits[i]?.memo || sp.memo || '', source: currentAccountId };
                      });
                      if (s.length > 0) setSplits(s);
                    }
                  }}>{d.text}</button>
                ))}
              </div>
            )}
          </div>
          <label className="block text-xs font-medium text-gray-500 mb-2 uppercase tracking-wide">{t('SPLITS', 'Splits')}</label>
          {splits.map((s, i) => <SplitRow key={i} split={s} index={i} isOnly={splits.length === 1} splitSources={splitSources} onUpdate={updSplit} onAdd={addSplit} onRemove={remSplit} />)}
        </div>
      </div>
    </div>
  );
}

// ── Root ─────────────────────────────────────────────────────────────────────
function MobileRoot() {
  const { refreshAccounts, refreshTransactions, refreshDescriptions, error, clearError, t } = useApp();
  const [view, setView] = useState('accounts');
  const [account, setAccount] = useState(null);
  const [transaction, setTransaction] = useState(null);
  const [confirmDialog, setConfirmDialog] = useState(null);

  function handleAccountSelect(acc) { setAccount(acc); setTransaction(null); setView('transactions'); }
  function handleNew() { setTransaction(null); setView('form'); }
  function handleSelectTx(tx) { setTransaction(tx); setView('form'); }
  function handleSaved() { refreshAccounts(); refreshTransactions(); refreshDescriptions(); setTransaction(null); setView('transactions'); }
  function handleDelete(id) {
    setConfirmDialog({ title: t('DELETE_TRANSACTION', 'Delete Transaction'), message: t('CONFIRM_DELETE_TRANSACTION_SIMPLE', 'Delete this transaction?'),
      onConfirm: async () => {
        try { await api.transactions.save({ action: 'delete', id }); refreshAccounts(); refreshTransactions(); refreshDescriptions(); setTransaction(null); setView('transactions'); } catch (_) {}
        setConfirmDialog(null);
      },
      onCancel: () => setConfirmDialog(null),
    });
  }

  return (
    <div className="fixed inset-0 overflow-hidden bg-white">
      {error && <AlertDialog open message={error} onClose={clearError} />}
      {confirmDialog && <ConfirmDialog open title={confirmDialog.title} message={confirmDialog.message} onConfirm={confirmDialog.onConfirm} onCancel={confirmDialog.onCancel} />}

      {/* Layer stack — each view is absolute, z-order via translate */}
      <AccountSidebar selectedAccount={account} onSelect={handleAccountSelect} visible={view === 'accounts'} />

      <div className={cn('absolute inset-0 transition-transform duration-300', view === 'accounts' ? 'translate-x-full' : 'translate-x-0')}>
        {view !== 'accounts' && (
          <TxListView account={account} onBack={() => setView('accounts')} onNew={handleNew} onSelect={handleSelectTx} />
        )}
      </div>

      <div className={cn('absolute inset-0 transition-transform duration-300', view === 'form' ? 'translate-x-0' : 'translate-x-full')}>
        {view === 'form' && (
          <TxFormView account={account} transaction={transaction} onSaved={handleSaved} onCancel={() => setView('transactions')} onDelete={handleDelete} />
        )}
      </div>
    </div>
  );
}

export default function MobileApp({ userConfig }) {
  return (
    <AppProvider userConfig={userConfig}>
      <MobileRoot />
    </AppProvider>
  );
}
