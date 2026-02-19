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
  { value: 'SCHEDULE_FREQUENCY_MONTHLY_BY_DATE', text: 'Monthly by Date' },
  { value: 'SCHEDULE_FREQUENCY_MONTHLY_BY_DAY_OF_WEEK', text: 'Monthly by Day of Week' },
  { value: 'SCHEDULE_FREQUENCY_WEEKLY', text: 'Weekly' },
  { value: 'SCHEDULE_FREQUENCY_BIWEEKLY', text: 'Bi-Weekly' },
  { value: 'SCHEDULE_FREQUENCY_EVERY_DAY', text: 'Every Day' },
  { value: 'SCHEDULE_FREQUENCY_EVERY_X_DAYS', text: 'Every X Days' },
  { value: 'SCHEDULE_FREQUENCY_EVERY_WEEKDAY', text: 'Every Weekday' },
  { value: 'SCHEDULE_FREQUENCY_MULTIPLE_WEEKS_EVERY_MONTH', text: 'Multiple Weeks Every Month' },
  { value: 'SCHEDULE_FREQUENCY_MULTIPLE_MONTHS_EVERY_YEAR', text: 'Multiple Months Every Year' },
];

const DAYS_OF_WEEK = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];
const MONTHS = ['January','February','March','April','May','June','July','August','September','October','November','December'];

function today() { return new Date().toISOString().split('T')[0]; }

export function ScheduledEditor({ open, selected, onClose, onSaved }) {
  const { showError } = useApp();
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
      <DialogContent title={isEditing ? 'Edit Scheduled Transaction' : 'Add Scheduled Transaction'} className="w-[700px]" onOk={handleSave} onCancel={onClose}>
        <div className="p-3 flex flex-col gap-2">
          <FormRow label="Name">
            <Input className="flex-1" value={name} onChange={e => setName(e.target.value)} autoFocus />
          </FormRow>
          <FormRow label="Frequency">
            <Select className="flex-1" value={repeat} onChange={e => setRepeat(e.target.value)} disabled={isEditing}>
              {FREQUENCIES.map(f => <option key={f.value} value={f.value}>{f.text}</option>)}
            </Select>
          </FormRow>
          <FormRow label="Start Date">
            <Input type="date" className="flex-1" value={startDate} onChange={e => setStartDate(e.target.value)} disabled={isEditing} />
          </FormRow>
          <FormRow label="End Date">
            <Input type="date" className="flex-1" value={endDate} onChange={e => setEndDate(e.target.value)} />
          </FormRow>

          {/* Frequency-specific config */}
          {(repeat === 'SCHEDULE_FREQUENCY_MONTHLY_BY_DATE') && (
            <FormRow label="Day of Month">
              <Select className="flex-1" value={scheduleDay} onChange={e => setScheduleDay(e.target.value)}>
                {Array.from({length: 31}, (_, i) => i + 1).map(d => <option key={d} value={d}>{d}</option>)}
                <option value={32}>Last Day</option>
              </Select>
            </FormRow>
          )}
          {(repeat === 'SCHEDULE_FREQUENCY_MONTHLY_BY_DAY_OF_WEEK' || repeat === 'SCHEDULE_FREQUENCY_WEEKLY' || repeat === 'SCHEDULE_FREQUENCY_BIWEEKLY') && (
            <FormRow label="Day of Week">
              <Select className="flex-1" value={scheduleDay} onChange={e => setScheduleDay(e.target.value)}>
                {DAYS_OF_WEEK.map((d, i) => <option key={i} value={i}>{d}</option>)}
              </Select>
            </FormRow>
          )}
          {repeat === 'SCHEDULE_FREQUENCY_EVERY_X_DAYS' && (
            <FormRow label="Every X Days">
              <Input type="number" min={1} className="w-24" value={scheduleDay} onChange={e => setScheduleDay(e.target.value)} />
            </FormRow>
          )}
          {repeat === 'SCHEDULE_FREQUENCY_MULTIPLE_MONTHS_EVERY_YEAR' && (
            <>
              <FormRow label="Day of Month">
                <Select className="flex-1" value={scheduleDay} onChange={e => setScheduleDay(e.target.value)}>
                  {Array.from({length: 31}, (_, i) => i + 1).map(d => <option key={d} value={d}>{d}</option>)}
                  <option value={32}>Last Day</option>
                </Select>
              </FormRow>
              <FormRow label="Months">
                <div className="flex flex-wrap gap-2">
                  {MONTHS.map((m, i) => (
                    <label key={i} className="flex items-center gap-1 text-xs cursor-pointer">
                      <input
                        type="checkbox"
                        checked={!!(scheduleMonth & (1 << i))}
                        onChange={() => toggleMonth(1 << i)}
                      />
                      {m.slice(0, 3)}
                    </label>
                  ))}
                </div>
              </FormRow>
            </>
          )}

          <FormRow label="Message">
            <Textarea className="flex-1" value={message} onChange={e => setMessage(e.target.value)} rows={2} />
          </FormRow>

          <div className="border-t border-gray-200 pt-2">
            <div className="text-xs font-semibold text-gray-600 mb-1">Transaction</div>
            <ScheduledTransactionMini splits={splits} onChange={setSplits} />
          </div>
        </div>
        <DialogFooter>
          <Button variant="default" onClick={onClose}>Cancel</Button>
          <Button variant="primary" disabled={!isValid || saving} onClick={handleSave}>OK</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function ScheduledTransactionMini({ splits, onChange }) {
  const { splitSources, setSplitSources, showError } = useApp();

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
        + Add split
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
