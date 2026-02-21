import { useState } from 'react';
import { Dialog, DialogContent, DialogFooter } from '../ui/Dialog';
import { Button } from '../ui/Button';
import { Select } from '../ui/Select';
import { DateField } from '../ui/DateField';
import { useApp } from '../../context/AppContext';
import {
  formatDateForDisplay,
  normalizeDateFormat,
  parseDisplayDate,
  parseIsoDate,
  todayIso,
  toIsoDate,
} from '../../lib/dateFormat';

const INTERVALS = [
  { value: 'PLUGIN_FILTER_THIS_WEEK', key: 'INTERVAL_THIS_WEEK', text: 'This Week' },
  { value: 'PLUGIN_FILTER_LAST_WEEK', key: 'INTERVAL_LAST_WEEK', text: 'Last Week' },
  { value: 'PLUGIN_FILTER_THIS_SEMI_MONTH', key: 'INTERVAL_THIS_SEMI_MONTH', text: 'This Semi-Month' },
  { value: 'PLUGIN_FILTER_LAST_SEMI_MONTH', key: 'INTERVAL_LAST_SEMI_MONTH', text: 'Last Semi-Month' },
  { value: 'PLUGIN_FILTER_THIS_MONTH', key: 'INTERVAL_THIS_MONTH', text: 'This Month' },
  { value: 'PLUGIN_FILTER_LAST_MONTH', key: 'INTERVAL_LAST_MONTH', text: 'Last Month' },
  { value: 'PLUGIN_FILTER_THIS_QUARTER', key: 'INTERVAL_THIS_QUARTER', text: 'This Quarter' },
  { value: 'PLUGIN_FILTER_LAST_QUARTER', key: 'INTERVAL_LAST_QUARTER', text: 'Last Quarter' },
  { value: 'PLUGIN_FILTER_THIS_YEAR', key: 'INTERVAL_THIS_YEAR', text: 'This Year' },
  { value: 'PLUGIN_FILTER_THIS_YEAR_TO_DATE', key: 'INTERVAL_THIS_YEAR_TO_DATE', text: 'This Year to Date' },
  { value: 'PLUGIN_FILTER_LAST_YEAR', key: 'INTERVAL_LAST_YEAR', text: 'Last Year' },
  { value: 'PLUGIN_FILTER_ALL_TIME', key: 'INTERVAL_ALL_TIME', text: 'All Time' },
  { value: 'PLUGIN_FILTER_OTHER', key: 'INTERVAL_CUSTOM', text: 'Custom Range...' },
];

export function IntervalPicker({ open, onClose, onConfirm }) {
  const { t, userConfig } = useApp();
  const dateFormat = normalizeDateFormat(userConfig?.dateFormat);
  const [interval, setInterval] = useState('PLUGIN_FILTER_THIS_MONTH');
  const [startDate, setStartDate] = useState(() => formatDateForDisplay(parseIsoDate(todayIso()), dateFormat));
  const [endDate, setEndDate] = useState(() => formatDateForDisplay(parseIsoDate(todayIso()), dateFormat));

  const isCustom = interval === 'PLUGIN_FILTER_OTHER';
  const startDateParsed = parseDisplayDate(startDate, dateFormat);
  const endDateParsed = parseDisplayDate(endDate, dateFormat);
  const startDateIso = startDateParsed ? toIsoDate(startDateParsed) : '';
  const endDateIso = endDateParsed ? toIsoDate(endDateParsed) : '';
  const isValid = !isCustom || (!!startDateIso && !!endDateIso && startDateIso <= endDateIso);

  function handleOk() {
    let query = `interval=${interval}`;
    const intervalItem = INTERVALS.find(i => i.value === interval);
    let dateRange = intervalItem ? t(intervalItem.key, intervalItem.text) : interval;
    if (isCustom) {
      query += `&startDate=${startDateIso}&endDate=${endDateIso}`;
      dateRange = `${startDate} - ${endDate}`;
    }
    onConfirm({ query, dateRange });
    onClose();
  }

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent title={t('SELECT_INTERVAL', 'Select Interval')} className="w-80" onOk={handleOk} onCancel={onClose}>
        <div className="p-3 flex flex-col gap-2">
          <FormRow label={t('INTERVAL', 'Interval')}>
            <Select className="flex-1" value={interval} onChange={e => setInterval(e.target.value)}>
              {INTERVALS.map(i => (
                <option key={i.value} value={i.value}>{t(i.key, i.text)}</option>
              ))}
            </Select>
          </FormRow>
          {isCustom && (
            <>
              <FormRow label={t('START_DATE', 'Start Date')}>
                <DateField className="flex-1" inputClassName="flex-1" value={startDate} onChange={setStartDate} />
              </FormRow>
              <FormRow label={t('END_DATE', 'End Date')}>
                <DateField className="flex-1" inputClassName="flex-1" value={endDate} onChange={setEndDate} />
              </FormRow>
            </>
          )}
        </div>
        <DialogFooter>
          <Button variant="default" onClick={onClose}>{t('CANCEL', 'Cancel')}</Button>
          <Button variant="primary" disabled={!isValid} onClick={handleOk}>{t('OK', 'OK')}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function FormRow({ label, children }) {
  return (
    <div className="flex items-center gap-2">
      <label className="text-xs text-gray-700 w-24 flex-shrink-0">{label}</label>
      {children}
    </div>
  );
}
