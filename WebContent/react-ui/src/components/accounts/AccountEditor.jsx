import { useState, useEffect } from 'react';
import { Dialog, DialogContent, DialogFooter } from '../ui/Dialog';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Select } from '../ui/Select';
import { api } from '../../lib/api';
import { useApp } from '../../context/AppContext';

export function AccountEditor({ open, selected, onClose, onSaved }) {
  const { showError, refreshAccounts } = useApp();
  const [name, setName] = useState('');
  const [accountType, setAccountType] = useState('');
  const [type, setType] = useState('D');
  const [startBalance, setStartBalance] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (open) {
      setName(selected?.name || '');
      setAccountType(selected?.accountType || '');
      setType(selected?.type || 'D');
      setStartBalance(selected?.startBalance || '');
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
        startBalance: parseFloat(startBalance) || 0,
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

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent title={selected ? 'Edit Account' : 'Add Account'} className="w-96" onOk={handleSave} onCancel={onClose}>
        <div className="p-3 flex flex-col gap-2">
          <FormRow label="Name">
            <Input
              className="flex-1"
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="e.g. Checking, Savings"
              autoFocus
            />
          </FormRow>
          <FormRow label="Account Type">
            <Input
              className="flex-1"
              value={accountType}
              onChange={e => setAccountType(e.target.value)}
              placeholder="e.g. Bank, Credit Card"
            />
          </FormRow>
          <FormRow label="Type">
            <Select className="flex-1" value={type} onChange={e => setType(e.target.value)}>
              <option value="D">Debit</option>
              <option value="C">Credit</option>
            </Select>
          </FormRow>
          <FormRow label="Starting Balance">
            <Input
              className="flex-1"
              type="text"
              inputMode="decimal"
              value={startBalance}
              onChange={e => setStartBalance(e.target.value)}
              onBlur={e => { const n = parseFloat(e.target.value); if (!isNaN(n)) setStartBalance(n.toFixed(2)); }}
              placeholder="0.00"
            />
          </FormRow>
          {selected && String(startBalance) !== String(selected.startBalance) && (
            <div className="ml-[8.5rem] text-xs text-red-600 font-semibold">
              ⚠ Changing the starting balance will affect all transaction balances for this account.
            </div>
          )}
        </div>
        <DialogFooter>
          <Button variant="default" onClick={onClose}>Cancel</Button>
          <Button variant="primary" disabled={!isValid || saving} onClick={handleSave}>OK</Button>
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
