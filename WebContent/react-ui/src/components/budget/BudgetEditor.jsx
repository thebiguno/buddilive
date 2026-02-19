import { useState, useEffect } from 'react';
import { Dialog, DialogContent, DialogFooter } from '../ui/Dialog';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Select } from '../ui/Select';
import { Combobox } from '../ui/Combobox';
import { api } from '../../lib/api';
import { useApp } from '../../context/AppContext';

export function BudgetEditor({ open, selected, onClose, onSaved }) {
  const { showError, t } = useApp();
  const [name, setName] = useState('');
  const [parentId, setParentId] = useState(null);
  const [periodType, setPeriodType] = useState('MONTH');
  const [type, setType] = useState('E');
  const [parentOptions, setParentOptions] = useState([]);
  const [saving, setSaving] = useState(false);
  const [parentDisabled, setParentDisabled] = useState(false);

  useEffect(() => {
    if (open) {
      setName(selected?.name || '');
      setParentId(selected?.parent || null);
      setPeriodType(selected?.periodType || 'MONTH');
      setType(selected?.categoryType || 'E');
      setParentDisabled(false);
    }
  }, [open, selected]);

  useEffect(() => {
    api.categories.parents(selected?.id)
      .then(data => {
        const opts = (data?.data || []).map(d => ({ value: d.value, text: d.text, periodType: d.periodType, type: d.type }));
        setParentOptions([{ value: '', text: t('NONE', '(None)') }, ...opts]);
      })
      .catch(() => {});
  }, [selected?.id, t]);

  function handleParentSelect(opt) {
    setParentId(opt.value || null);
    if (opt.value) {
      if (opt.periodType) setPeriodType(opt.periodType);
      if (opt.type) setType(opt.type);
      setParentDisabled(true);
    } else {
      setParentDisabled(false);
    }
  }

  const isValid = name.trim().length > 0;

  async function handleSave() {
    if (!isValid) return;
    setSaving(true);
    try {
      await api.categories.save({
        action: selected ? 'update' : 'insert',
        id: selected?.id,
        name: name.trim(),
        parent: parentId || null,
        periodType,
        type,
      });
      onClose();
      onSaved && onSaved();
    } catch (e) {
      showError(e);
    } finally {
      setSaving(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent title={selected ? t('MODIFY_BUDGET_CATEGORY', 'Edit Budget Category') : t('NEW_BUDGET_CATEGORY', 'Add Budget Category')} className="w-96" onOk={handleSave} onCancel={onClose}>
        <div className="p-3 flex flex-col gap-2">
          <FormRow label={t('NAME', 'Name')}>
            <Input
              className="flex-1"
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder={t('BUDGET_CATEGORY_NAME_EXAMPLES', 'e.g. Groceries, Rent')}
              autoFocus
            />
          </FormRow>
          <FormRow label={t('PARENT', 'Parent')}>
            <Combobox
              className="flex-1"
              options={parentOptions}
              value={parentId || ''}
              onSelect={handleParentSelect}
              placeholder={t('NONE', '(None)')}
            />
          </FormRow>
          {!selected && (
            <>
              <FormRow label={t('PERIOD_TYPE', 'Period Type')}>
                <Select className="flex-1" value={periodType} onChange={e => setPeriodType(e.target.value)} disabled={parentDisabled}>
                  <option value="WEEK">{t('PERIOD_WEEKLY', 'Weekly')}</option>
                  <option value="SEMI_MONTH">{t('PERIOD_SEMI_MONTHLY', 'Semi-Monthly')}</option>
                  <option value="MONTH">{t('PERIOD_MONTHLY', 'Monthly')}</option>
                  <option value="QUARTER">{t('PERIOD_QUARTERLY', 'Quarterly')}</option>
                  <option value="SEMI_YEAR">{t('PERIOD_SEMI_YEARLY', 'Semi-Yearly')}</option>
                  <option value="YEAR">{t('PERIOD_YEARLY', 'Yearly')}</option>
                </Select>
              </FormRow>
              <FormRow label={t('TYPE', 'Type')}>
                <Select className="flex-1" value={type} onChange={e => setType(e.target.value)} disabled={parentDisabled}>
                  <option value="I">{t('INCOME', 'Income')}</option>
                  <option value="E">{t('EXPENSE', 'Expense')}</option>
                </Select>
              </FormRow>
            </>
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
      <label className="text-xs text-gray-700 w-28 flex-shrink-0">{label}</label>
      {children}
    </div>
  );
}
