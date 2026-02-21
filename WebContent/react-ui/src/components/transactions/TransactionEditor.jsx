import { useState, useEffect, useRef } from 'react';
import { Combobox } from '../ui/Combobox';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { SplitEditor } from './SplitEditor';
import { ConfirmDialog } from '../ui/ConfirmDialog';
import { api } from '../../lib/api';
import { useApp } from '../../context/AppContext';

const EMPTY_SPLIT = () => ({ amount: '', fromId: null, toId: null, memo: '' });

export function TransactionEditor({ selectedAccount, selectedTransaction, onSaved, onClear, onDelete }) {
  const { splitSources, setSplitSources, showError, descriptionStoreVersion, t } = useApp();
  const [date, setDate] = useState(today());
  const [description, setDescription] = useState('');
  const [number, setNumber] = useState('');
  const [splits, setSplits] = useState([EMPTY_SPLIT()]);
  const [transactionId, setTransactionId] = useState(null);
  const [descriptions, setDescriptions] = useState([]);
  const [saving, setSaving] = useState(false);
  const [confirmPending, setConfirmPending] = useState(null);
  // Snapshot of the transaction as it was loaded, for change detection
  const loadedTransaction = useRef(null);
  const dateInputRef = useRef(null);
  const descriptionInputRef = useRef(null);

  // Load split sources once
  useEffect(() => {
    if (splitSources.from.length === 0) {
      api.transactions.splitSources().then(setSplitSources).catch(showError);
    }
  }, [splitSources.from.length, setSplitSources, showError]);

  // Load description autocomplete
  useEffect(() => {
    api.transactions.descriptions()
      .then(data => setDescriptions((data?.data || []).map(d => ({ value: d.value, text: d.value, transaction: d.transaction }))))
      .catch(() => {});
  }, [descriptionStoreVersion]);

  // Load selected transaction into form
  useEffect(() => {
    if (selectedTransaction) {
      const id = selectedTransaction.id || null;
      const d = selectedTransaction.dateIso || today();
      const desc = selectedTransaction.description || '';
      const num = selectedTransaction.number || '';
      const s = selectedTransaction.splits || [];
      const mappedSplits = s.length > 0 ? s.map(sp => ({
        amount: sp.amountNumber || sp.amount || '',
        fromId: sp.fromId || null,
        toId: sp.toId || null,
        memo: sp.memo || '',
        source: selectedAccount?.id,
      })) : [{ ...EMPTY_SPLIT(), source: selectedAccount?.id }];
      setTransactionId(id);
      setDate(d);
      setDescription(desc);
      setNumber(num);
      setSplits(mappedSplits);
      loadedTransaction.current = { id, date: d, description: desc, number: num, splitsCount: mappedSplits.length };
    } else {
      clearForm(false);
      loadedTransaction.current = null;
    }
  }, [selectedTransaction]);

  // When account changes, update source on splits
  useEffect(() => {
    setSplits(prev => prev.map(s => ({ ...s, source: selectedAccount?.id })));
  }, [selectedAccount]);

  function clearForm(preserveDate = false) {
    setTransactionId(null);
    if (!preserveDate) setDate(today());
    setDescription('');
    setNumber('');
    setSplits([{ ...EMPTY_SPLIT(), source: selectedAccount?.id }]);
  }

  function remapSplitToCurrentAccount(sp, currentAccountId) {
    const isAccountType = t => t === 'C' || t === 'D';
    const fromIsAccount = isAccountType(sp.fromType);
    const toIsAccount = isAccountType(sp.toType);
    let fromId = sp.fromId || null;
    let toId = sp.toId || null;
    if (fromIsAccount && toIsAccount) {
      fromId = currentAccountId;
    } else if (fromIsAccount) {
      fromId = currentAccountId;
    } else if (toIsAccount) {
      toId = currentAccountId;
    }
    return { fromId, toId };
  }

  function ensureDistinctSplitSources(fromId, toId, originalFromId, originalToId) {
    if (fromId == null || toId == null || fromId !== toId) {
      return { fromId, toId };
    }

    // Prefer restoring the original opposite side if it keeps the pair distinct.
    if (originalFromId != null && originalFromId !== toId) {
      return { fromId: originalFromId, toId };
    }
    if (originalToId != null && originalToId !== fromId) {
      return { fromId, toId: originalToId };
    }

    // Final fallback: force user to pick a different destination source.
    return { fromId, toId: null };
  }

  function handleDescriptionSelect(opt) {
    setDescription(opt.value);
    if (opt.transaction) {
      const t = opt.transaction;
      const currentAccountId = selectedAccount?.id || null;
      const s = (t.splits || []).map((sp, i) => {
        const existing = splits[i] || {};
        const originalFromId = sp.fromId || null;
        const originalToId = sp.toId || null;
        let { fromId, toId } = remapSplitToCurrentAccount(sp, currentAccountId);
        ({ fromId, toId } = ensureDistinctSplitSources(fromId, toId, originalFromId, originalToId));
        return {
          amount: existing.amount || sp.amountNumber || sp.amount || '',
          fromId,
          toId,
          memo: existing.memo || sp.memo || '',
          source: currentAccountId,
        };
      });
      if (s.length > 0) setSplits(s);
    }
  }

  function updateSplit(index, updated) {
    setSplits(prev => prev.map((s, i) => i === index ? updated : s));
  }

  function addSplit(afterIndex) {
    setSplits(prev => [
      ...prev.slice(0, afterIndex + 1),
      { ...EMPTY_SPLIT(), source: selectedAccount?.id },
      ...prev.slice(afterIndex + 1),
    ]);
  }

  function removeSplit(index) {
    setSplits(prev => prev.filter((_, i) => i !== index));
  }

  function isValid() {
    if (!date) return false;
    if (!description.trim()) return false;
    for (const s of splits) {
      if (!s.amount || parseFloat(s.amount) === 0) return false;
      if (!s.fromId || !s.toId) return false;
    }
    return true;
  }

  async function doSave() {
    setSaving(true);
    try {
      await api.transactions.save({
        action: transactionId ? 'update' : 'insert',
        ...(transactionId ? { id: transactionId } : {}),
        date,
        description: description.trim(),
        number,
        splits: splits.map(s => ({
          amount: parseFloat(s.amount),
          fromId: s.fromId,
          toId: s.toId,
          memo: s.memo || '',
        })),
      });
      clearForm(true);
      if (onSaved) {
        await onSaved();
      }
      requestAnimationFrame(() => dateInputRef.current?.focus());
    } catch (e) {
      showError(e);
    } finally {
      setSaving(false);
    }
  }

  function handleSave() {
    if (!isValid()) return;

    const d = new Date(date + 'T00:00:00');
    const now = new Date();
    const future = new Date(now); future.setDate(future.getDate() + 7);
    const past = new Date(now); past.setMonth(past.getMonth() - 3);

    const dateOutOfRange = d > future || d < past;
    const loaded = loadedTransaction.current;
    const isModify = !!transactionId && loaded;
    const fieldsChanged = isModify && (
      loaded.date !== date ||
      loaded.description !== description.trim() ||
      loaded.number !== (number || '') ||
      loaded.splitsCount !== splits.length
    );

    if (dateOutOfRange) {
      const msg = d > future
        ? t('TRANSACTION_DATE_TOO_FAR_FUTURE', 'The date is more than 7 days in the future. Are you sure you want to save this transaction?')
        : t('TRANSACTION_DATE_TOO_FAR_PAST', 'The date is more than 3 months in the past. Are you sure you want to save this transaction?');
      setConfirmPending({
        title: t('DATE_OUT_OF_RANGE', 'Date Out of Range'),
        message: msg,
        onConfirm: () => {
          setConfirmPending(null);
          if (fieldsChanged) {
            setConfirmPending({
              title: t('MODIFY_EXISTING_TRANSACTION', 'Modify Existing Transaction'),
              message: t('MODIFY_EXISTING_TRANSACTION_CONFIRM', 'You have changed fields on an existing transaction. Are you sure you want to save these changes?'),
              onConfirm: () => { setConfirmPending(null); doSave(); },
              onCancel: () => setConfirmPending(null),
            });
          } else {
            doSave();
          }
        },
        onCancel: () => setConfirmPending(null),
      });
    } else if (fieldsChanged) {
      setConfirmPending({
        title: t('MODIFY_EXISTING_TRANSACTION', 'Modify Existing Transaction'),
        message: t('MODIFY_EXISTING_TRANSACTION_CONFIRM', 'You have changed fields on an existing transaction. Are you sure you want to save these changes?'),
        onConfirm: () => { setConfirmPending(null); doSave(); },
        onCancel: () => setConfirmPending(null),
      });
    } else {
      doSave();
    }
  }

  async function handleDelete() {
    if (!transactionId) return;
    onDelete && onDelete(transactionId);
  }

  function handleClear() {
    clearForm(false);
    onClear && onClear();
  }

  const sourcesWithAccount = {
    from: splitSources.from,
    to: splitSources.to,
  };

  function handleKeyDown(e) {
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      e.preventDefault();
      handleSave();
    }
  }

  function handleDateKeyDown(e) {
    if (e.key === 'Tab') {
      e.preventDefault();
      if (e.shiftKey) {
        focusPreviousTabStop(dateInputRef.current);
      } else {
        descriptionInputRef.current?.focus();
      }
    }
  }

  function focusPreviousTabStop(current) {
    if (!current) return;
    const selector = [
      'a[href]',
      'button:not([disabled])',
      'input:not([disabled]):not([type="hidden"])',
      'select:not([disabled])',
      'textarea:not([disabled])',
      '[tabindex]:not([tabindex="-1"])',
    ].join(',');

    const tabbables = Array.from(document.querySelectorAll(selector)).filter(el => {
      if (!(el instanceof HTMLElement)) return false;
      if (el.hasAttribute('disabled')) return false;
      const style = window.getComputedStyle(el);
      return style.display !== 'none' && style.visibility !== 'hidden';
    });

    const index = tabbables.indexOf(current);
    if (index > 0) {
      tabbables[index - 1].focus();
    }
  }

  return (
    <div className="relative border-b border-gray-300 bg-[#f0f0f0] px-2 py-1.5" onKeyDown={handleKeyDown}>
      {confirmPending && (
        <ConfirmDialog
          open
          title={confirmPending.title}
          message={confirmPending.message}
          onConfirm={confirmPending.onConfirm}
          onCancel={confirmPending.onCancel}
        />
      )}
      {saving && (
        <div className="absolute inset-0 z-20 bg-white/65 flex items-center justify-center">
          <div className="px-3 py-1.5 text-sm font-semibold text-gray-700 bg-white border border-gray-300 rounded shadow-sm">
            {t('SAVING', 'Saving...')}
          </div>
        </div>
      )}
      {/* Top row: date, description, number */}
      <div className="flex items-center gap-2 mb-1">
        <Input
          ref={dateInputRef}
          type="date"
          className="w-32"
          value={date}
          onChange={e => setDate(e.target.value)}
          onKeyDown={handleDateKeyDown}
        />
        <Combobox
          ref={descriptionInputRef}
          className="flex-1"
          options={descriptions}
          value={description}
          onChange={setDescription}
          onSelect={handleDescriptionSelect}
          placeholder={t('DESCRIPTION', 'Description')}
        />
        <Input
          className="w-56"
          value={number}
          onChange={e => setNumber(e.target.value)}
          placeholder={t('NUMBER', 'Number')}
        />
      </div>
      {/* Split rows */}
      <div className="flex flex-col gap-0.5">
        {splits.map((split, i) => (
          <SplitEditor
            key={i}
            split={split}
            index={i}
            isOnly={splits.length === 1}
            splitSources={sourcesWithAccount}
            onUpdate={updateSplit}
            onAdd={addSplit}
            onRemove={removeSplit}
          />
        ))}
      </div>
      {/* Bottom toolbar */}
      <div className="flex items-center gap-2 mt-1.5">
        <Button
          variant="danger"
          disabled={!transactionId}
          onClick={handleDelete}
        >
          {t('DELETE_TRANSACTION', 'Delete Transaction')}
        </Button>
        <div className="flex-1" />
        <Button variant="default" onClick={handleClear}>{t('CLEAR', 'Clear')}</Button>
        <Button
          variant="primary"
          disabled={!isValid() || saving}
          onClick={handleSave}
        >
          {transactionId ? t('UPDATE', 'Update') : t('RECORD', 'Record')} {t('TRANSACTION', 'Transaction')}
        </Button>
      </div>
    </div>
  );
}

function today() {
  return new Date().toISOString().split('T')[0];
}
