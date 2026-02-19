import { useState, useEffect } from 'react';
import { Dialog, DialogContent, DialogFooter } from '../ui/Dialog';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Select } from '../ui/Select';
import { Textarea } from '../ui/Input';
import { SplitEditor } from '../transactions/SplitEditor';
import { api } from '../../lib/api';
import { useApp } from '../../context/AppContext';

const FREQUENCIES = [
  { value: 'SCHEDULE_FREQUENCY_MONTHLY_BY_DATE', key: 'SCHEDULE_FREQUENCY_MONTHLY_BY_DATE_LABEL', text: 'Monthly by Date' },
  { value: 'SCHEDULE_FREQUENCY_MONTHLY_BY_DAY_OF_WEEK', key: 'SCHEDULE_FREQUENCY_MONTHLY_BY_DAY_OF_WEEK_LABEL', text: 'Monthly by Day of Week' },
  { value: 'SCHEDULE_FREQUENCY_WEEKLY', key: 'SCHEDULE_FREQUENCY_WEEKLY_LABEL', text: 'Weekly' },
  { value: 'SCHEDULE_FREQUENCY_BIWEEKLY', key: 'SCHEDULE_FREQUENCY_BIWEEKLY_LABEL', text: 'Bi-Weekly' },
  { value: 'SCHEDULE_FREQUENCY_EVERY_DAY', key: 'SCHEDULE_FREQUENCY_EVERY_DAY_LABEL', text: 'Every Day' },
  { value: 'SCHEDULE_FREQUENCY_EVERY_X_DAYS', key: 'SCHEDULE_FREQUENCY_EVERY_X_DAYS_LABEL', text: 'Every X Days' },
  { value: 'SCHEDULE_FREQUENCY_EVERY_WEEKDAY', key: 'SCHEDULE_FREQUENCY_EVERY_WEEKDAY_LABEL', text: 'Every Weekday' },
  { value: 'SCHEDULE_FREQUENCY_MULTIPLE_WEEKS_EVERY_MONTH', key: 'SCHEDULE_FREQUENCY_MULTIPLE_WEEKS_EVERY_MONTH_LABEL', text: 'Multiple Weeks Every Month' },
  { value: 'SCHEDULE_FREQUENCY_MULTIPLE_MONTHS_EVERY_YEAR', key: 'SCHEDULE_FREQUENCY_MULTIPLE_MONTHS_EVERY_YEAR_LABEL', text: 'Multiple Months Every Year' },
];

const DAYS_OF_WEEK = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];
const MONTHS = ['January','February','March','April','May','June','July','August','September','October','November','December'];

function today() { return new Date().toISOString().split('T')[0]; }

export function ScheduledEditor({ open, selected, onClose, onSaved }) {
  const { showError, t } = useApp();
  const [name, setName] = useState(selected?.name || '');
  const [repeat, setRepeat] = useState(selected?.repeat || 'SCHEDULE_FREQUENCY_MONTHLY_BY_DATE');
  const [startDate, setStartDate] = useState(selected?.start || today());
  const [endDate, setEndDate] = useState(selected?.end || '');
  const [scheduleDay, setScheduleDay] = useState(selected?.scheduleDay ?? 1);
  const [scheduleWeek, setScheduleWeek] = useState(selected?.scheduleWeek ?? 0);
  const [scheduleMonth, setScheduleMonth] = useState(selected?.scheduleMonth ?? 0);
  const [description, setDescription] = useState(selected?.description || '');
  const [message, setMessage] = useState(selected?.message || '');
  const [splits, setSplits] = useState(selected?.splits || []);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!open) return;
    setName(selected?.name || '');
    setRepeat(selected?.repeat || 'SCHEDULE_FREQUENCY_MONTHLY_BY_DATE');
    setStartDate(selected?.start || today());
    setEndDate(selected?.end || '');
    setScheduleDay(selected?.scheduleDay ?? 1);
    setScheduleWeek(selected?.scheduleWeek ?? 0);
    setScheduleMonth(selected?.scheduleMonth ?? 0);
    setDescription(selected?.description || '');
    setMessage(selected?.message || '');
    setSplits(selected?.splits || []);
    setSaving(false);
  }, [open, selected]);

  const isEditing = !!selected;
  const isValid = name.trim().length > 0;

  function toggleMonth(bit) {
    setScheduleMonth(m => m ^ bit);
  }

  async function handleSave() {
    if (!isValid) return;
    setSaving(true);
    try {
      await api.scheduled.save({
        action: isEditing ? 'update' : 'insert',
        id: selected?.id ?? '',
        lastCreatedDate: selected?.lastCreatedDate || null,
        name: name.trim(),
        repeat,
        start: startDate,
        end: endDate || null,
        scheduleDay: parseInt(scheduleDay) || 1,
        scheduleWeek: parseInt(scheduleWeek) || 0,
        scheduleMonth,
        message,
        transaction: {
          description: description || name.trim(),
          splits,
        },
      });
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
      <DialogContent title={isEditing ? t('MODIFY_SCHEDULED_TRANSACTION', 'Edit Scheduled Transaction') : t('NEW_SCHEDULED_TRANSACTION', 'Add Scheduled Transaction')} className="w-[700px]" onOk={handleSave} onCancel={onClose}>
        <div className="p-3 flex flex-col gap-2">
          <FormRow label={t('NAME', 'Name')}>
            <Input className="flex-1" value={name} onChange={e => setName(e.target.value)} autoFocus />
          </FormRow>
          <FormRow label={t('FREQUENCY', 'Frequency')}>
            <Select className="flex-1" value={repeat} onChange={e => setRepeat(e.target.value)} disabled={isEditing}>
              {FREQUENCIES.map(f => <option key={f.value} value={f.value}>{t(f.key, f.text)}</option>)}
            </Select>
          </FormRow>
          <FormRow label={t('START_DATE', 'Start Date')}>
            <Input type="date" className="flex-1" value={startDate} onChange={e => setStartDate(e.target.value)} disabled={isEditing} />
          </FormRow>
          <FormRow label={t('END_DATE', 'End Date')}>
            <Input type="date" className="flex-1" value={endDate} onChange={e => setEndDate(e.target.value)} />
          </FormRow>

          {/* Frequency-specific config */}
          {(repeat === 'SCHEDULE_FREQUENCY_MONTHLY_BY_DATE') && (
            <FormRow label={t('DAY_OF_MONTH', 'Day of Month')}>
              <Select className="flex-1" value={scheduleDay} onChange={e => setScheduleDay(e.target.value)}>
                {Array.from({length: 31}, (_, i) => i + 1).map(d => <option key={d} value={d}>{d}</option>)}
                <option value={32}>{t('LAST_DAY', 'Last Day')}</option>
              </Select>
            </FormRow>
          )}
          {(repeat === 'SCHEDULE_FREQUENCY_MONTHLY_BY_DAY_OF_WEEK' || repeat === 'SCHEDULE_FREQUENCY_WEEKLY' || repeat === 'SCHEDULE_FREQUENCY_BIWEEKLY') && (
            <FormRow label={t('DAY_OF_WEEK', 'Day of Week')}>
              <Select className="flex-1" value={scheduleDay} onChange={e => setScheduleDay(e.target.value)}>
                {DAYS_OF_WEEK.map((d, i) => <option key={i} value={i}>{t(`DAY_NAME_${i}`, d)}</option>)}
              </Select>
            </FormRow>
          )}
          {repeat === 'SCHEDULE_FREQUENCY_EVERY_X_DAYS' && (
            <FormRow label={t('EVERY_X_DAYS', 'Every X Days')}>
              <Input type="number" min={1} className="w-24" value={scheduleDay} onChange={e => setScheduleDay(e.target.value)} />
            </FormRow>
          )}
          {repeat === 'SCHEDULE_FREQUENCY_MULTIPLE_MONTHS_EVERY_YEAR' && (
            <>
              <FormRow label={t('DAY_OF_MONTH', 'Day of Month')}>
                <Select className="flex-1" value={scheduleDay} onChange={e => setScheduleDay(e.target.value)}>
                  {Array.from({length: 31}, (_, i) => i + 1).map(d => <option key={d} value={d}>{d}</option>)}
                  <option value={32}>{t('LAST_DAY', 'Last Day')}</option>
                </Select>
              </FormRow>
              <FormRow label={t('MONTHS', 'Months')}>
                <div className="flex flex-wrap gap-2">
                  {MONTHS.map((m, i) => (
                    <label key={i} className="flex items-center gap-1 text-xs cursor-pointer">
                      <input
                        type="checkbox"
                        checked={!!(scheduleMonth & (1 << i))}
                        onChange={() => toggleMonth(1 << i)}
                      />
                      {t(`MONTH_NAME_SHORT_${i}`, m.slice(0, 3))}
                    </label>
                  ))}
                </div>
              </FormRow>
            </>
          )}

          <FormRow label={t('MESSAGE', 'Message')}>
            <Textarea className="flex-1" value={message} onChange={e => setMessage(e.target.value)} rows={2} />
          </FormRow>

          <div className="border-t border-gray-200 pt-2">
            <div className="text-xs font-semibold text-gray-600 mb-1">{t('TRANSACTION', 'Transaction')}</div>
            <ScheduledTransactionMini splits={splits} onChange={setSplits} />
          </div>
        </div>
        <DialogFooter>
          <Button variant="default" onClick={onClose}>{t('CANCEL', 'Cancel')}</Button>
          <Button variant="primary" disabled={!isValid || saving} onClick={handleSave}>{t('OK', 'OK')}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function ScheduledTransactionMini({ splits, onChange }) {
  const { splitSources, setSplitSources, showError, t } = useApp();

  useEffect(() => {
    if (splitSources.from.length === 0) {
      api.transactions.splitSources().then(setSplitSources).catch(showError);
    }
  }, [splitSources.from.length, setSplitSources, showError]);

  function updateSplit(i, updated) {
    onChange(prev => prev.map((s, idx) => idx === i ? updated : s));
  }
  function addSplit(afterIndex) {
    onChange(prev => [
      ...prev.slice(0, afterIndex + 1),
      { amount: '', fromId: null, toId: null, memo: '' },
      ...prev.slice(afterIndex + 1),
    ]);
  }
  function removeSplit(i) {
    onChange(prev => prev.filter((_, idx) => idx !== i));
  }

  if (splits.length === 0) {
    return (
      <button
        className="text-xs text-blue-600 hover:underline cursor-pointer"
        onClick={() => onChange([{ amount: '', fromId: null, toId: null, memo: '' }])}
      >
        + {t('ADD_SPLIT', 'Add split')}
      </button>
    );
  }

  return splits.map((split, i) => (
    <SplitEditor
      key={i}
      split={split}
      index={i}
      isOnly={splits.length === 1}
      splitSources={splitSources}
      onUpdate={updateSplit}
      onAdd={addSplit}
      onRemove={removeSplit}
    />
  ));
}

function FormRow({ label, children }) {
  return (
    <div className="flex items-start gap-2">
      <label className="text-xs text-gray-700 w-28 flex-shrink-0 pt-0.5">{label}</label>
      <div className="flex-1">{children}</div>
    </div>
  );
}
