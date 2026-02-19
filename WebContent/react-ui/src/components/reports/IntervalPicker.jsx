import { useState } from 'react';
import { Dialog, DialogContent, DialogFooter } from '../ui/Dialog';
import { Button } from '../ui/Button';
import { Select } from '../ui/Select';
import { Input } from '../ui/Input';

const INTERVALS = [
  { value: 'PLUGIN_FILTER_THIS_WEEK', text: 'This Week' },
  { value: 'PLUGIN_FILTER_LAST_WEEK', text: 'Last Week' },
  { value: 'PLUGIN_FILTER_THIS_SEMI_MONTH', text: 'This Semi-Month' },
  { value: 'PLUGIN_FILTER_LAST_SEMI_MONTH', text: 'Last Semi-Month' },
  { value: 'PLUGIN_FILTER_THIS_MONTH', text: 'This Month' },
  { value: 'PLUGIN_FILTER_LAST_MONTH', text: 'Last Month' },
  { value: 'PLUGIN_FILTER_THIS_QUARTER', text: 'This Quarter' },
  { value: 'PLUGIN_FILTER_LAST_QUARTER', text: 'Last Quarter' },
  { value: 'PLUGIN_FILTER_THIS_YEAR', text: 'This Year' },
  { value: 'PLUGIN_FILTER_THIS_YEAR_TO_DATE', text: 'This Year to Date' },
  { value: 'PLUGIN_FILTER_LAST_YEAR', text: 'Last Year' },
  { value: 'PLUGIN_FILTER_ALL_TIME', text: 'All Time' },
  { value: 'PLUGIN_FILTER_OTHER', text: 'Custom Range...' },
];

export function IntervalPicker({ open, onClose, onConfirm }) {
  const [interval, setInterval] = useState('PLUGIN_FILTER_THIS_MONTH');
  const [startDate, setStartDate] = useState(today());
  const [endDate, setEndDate] = useState(today());

  const isCustom = interval === 'PLUGIN_FILTER_OTHER';
  const isValid = !isCustom || (startDate && endDate && startDate <= endDate);

  function handleOk() {
    let query = `interval=${interval}`;
    let dateRange = INTERVALS.find(i => i.value === interval)?.text || interval;
    if (isCustom) {
      query += `&startDate=${startDate}&endDate=${endDate}`;
      dateRange = `${startDate} - ${endDate}`;
    }
    onConfirm({ query, dateRange });
    onClose();
  }

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent title="Select Interval" className="w-80" onOk={handleOk} onCancel={onClose}>
        <div className="p-3 flex flex-col gap-2">
          <FormRow label="Interval">
            <Select className="flex-1" value={interval} onChange={e => setInterval(e.target.value)}>
              {INTERVALS.map(i => (
                <option key={i.value} value={i.value}>{i.text}</option>
              ))}
            </Select>
          </FormRow>
          {isCustom && (
            <>
              <FormRow label="Start Date">
                <Input
                  type="date"
                  className="flex-1"
                  value={startDate}
                  onChange={e => setStartDate(e.target.value)}
                  max={endDate}
                />
              </FormRow>
              <FormRow label="End Date">
                <Input
                  type="date"
                  className="flex-1"
                  value={endDate}
                  onChange={e => setEndDate(e.target.value)}
                  min={startDate}
                />
              </FormRow>
            </>
          )}
        </div>
        <DialogFooter>
          <Button variant="default" onClick={onClose}>Cancel</Button>
          <Button variant="primary" disabled={!isValid} onClick={handleOk}>OK</Button>
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

function today() {
  return new Date().toISOString().split('T')[0];
}
