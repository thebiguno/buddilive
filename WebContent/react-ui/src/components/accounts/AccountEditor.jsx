import { useState, useEffect } from 'react';
import { Dialog, DialogContent, DialogFooter } from '../ui/Dialog';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Select } from '../ui/Select';
import { api } from '../../lib/api';
import { useApp } from '../../context/AppContext';
import { formatLocaleNumber, getAmountPlaceholder, parseLocaleNumber } from '../../lib/numberFormat';

export function AccountEditor({ open, selected, onClose, onSaved }) {
  const { showError, refreshAccounts, t, userConfig } = useApp();
  const locale = userConfig?.locale;
  const [name, setName] = useState('');
  const [accountType, setAccountType] = useState('');
  const [type, setType] = useState('D');
  const [startBalance, setStartBalance] = useState('');
  const [saving, setSaving] = useState(false);

  function formatBalance(v) {
    const n = parseLocaleNumber(v, locale);
    return Number.isFinite(n)
      ? formatLocaleNumber(n, locale, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
      : '';
  }

  useEffect(() => {
    if (open) {
      setName(selected?.name || '');
      setAccountType(selected?.accountType || '');
      setType(selected?.type || 'D');
      setStartBalance(formatBalance(selected?.startBalance));
    }
  }, [open, selected]);

  const isValid = name.trim().length > 0 && accountType.trim().length > 0;

  async function handleSave() {
    if (!isValid) return;
    setSaving(true);
    try {
      await api.accounts.save({
        action: selected ? 'update' : 'insert',
        id: selected?.id,
        name: name.trim(),
        accountType: accountType.trim(),
        type,
        startBalance: parseLocaleNumber(startBalance, locale) || 0,
      });
      refreshAccounts();
      onSaved && onSaved();
      onClose();
    } catch (e) {
      showError(e);
    } finally {
      setSaving(false);
    }
  }

  const hasStartBalanceChanged = selected && formatBalance(startBalance) !== formatBalance(selected.startBalance);

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent title={selected ? t('EDIT_ACCOUNT', 'Edit Account') : t('NEW_ACCOUNT', 'Add Account')} className="w-96" onOk={handleSave} onCancel={onClose}>
        <div className="p-3 flex flex-col gap-2">
          <FormRow label={t('ACCOUNT_EDITOR_NAME', 'Name')}>
            <Input
              className="flex-1"
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder={t('ACCOUNT_EDITOR_NAME_EXAMPLES', 'e.g. Checking, Savings')}
              autoFocus
            />
          </FormRow>
          <FormRow label={t('ACCOUNT_EDITOR_ACCOUNT_TYPE', 'Account Type')}>
            <Input
              className="flex-1"
              value={accountType}
              onChange={e => setAccountType(e.target.value)}
              placeholder={t('ACCOUNT_EDITOR_ACCOUNT_TYPE_EXAMPLES', 'e.g. Bank, Credit Card')}
            />
          </FormRow>
          <FormRow label={t('ACCOUNT_EDITOR_TYPE', 'Type')}>
            <Select className="flex-1" value={type} onChange={e => setType(e.target.value)}>
              <option value="D">{t('DEBIT', 'Debit')}</option>
              <option value="C">{t('CREDIT', 'Credit')}</option>
            </Select>
          </FormRow>
          <FormRow label={t('ACCOUNT_EDITOR_STARTING_BALANCE', 'Starting Balance')}>
            <Input
              className="flex-1"
              type="text"
              inputMode="decimal"
              value={startBalance}
              onChange={e => setStartBalance(e.target.value)}
              onBlur={e => {
                const n = parseLocaleNumber(e.target.value, locale);
                if (Number.isFinite(n)) {
                  setStartBalance(formatLocaleNumber(n, locale, { minimumFractionDigits: 2, maximumFractionDigits: 2 }));
                }
              }}
              placeholder={getAmountPlaceholder(locale)}
            />
          </FormRow>
          {hasStartBalanceChanged && (
            <div className="ml-[8.5rem] text-xs text-red-600 font-semibold">
              ⚠ {t('ACCOUNT_EDITOR_STARTING_BALANCE_WARNING', 'Changing the starting balance will affect all transaction balances for this account.')}
            </div>
          )}
        </div>
        <DialogFooter>
          <Button variant="default" onClick={onClose}>{t('CANCEL', 'Cancel')}</Button>
          <Button variant="primary" disabled={!isValid || saving} onClick={handleSave}>{t('OK', 'OK')}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function FormRow({ label, children }) {
  return (
    <div className="flex items-center gap-2">
      <label className="text-xs text-gray-700 w-32 flex-shrink-0">{label}</label>
      {children}
    </div>
  );
}
