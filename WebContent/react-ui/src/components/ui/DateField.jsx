import { forwardRef, useEffect, useMemo, useRef, useState } from 'react';
import { Calendar, ChevronLeft, ChevronRight } from 'lucide-react';
import { Input } from './Input';
import { useApp } from '../../context/AppContext';
import { cn } from '../../lib/utils';
import {
  formatDateForDisplay,
  getDatePlaceholder,
  normalizeDateFormat,
  normalizeDateInput,
  parseDisplayDate,
  parseIsoDate,
  toIsoDate,
  todayIso,
} from '../../lib/dateFormat';

export const DateField = forwardRef(function DateField({
  value,
  onChange,
  className,
  inputClassName,
  disabled,
  ariaLabel,
  title,
  onKeyDown,
  onFocus,
  onBlur,
  onTabNext,
  onTabPrevious,
  dateFormat: dateFormatOverride,
}, ref) {
  const { userConfig, t } = useApp();
  const dateFormat = normalizeDateFormat(dateFormatOverride ?? userConfig?.dateFormat);
  const containerRef = useRef(null);
  const [datePickerOpen, setDatePickerOpen] = useState(false);
  const [pickerMonth, setPickerMonth] = useState(() => {
    const d = parseIsoDate(todayIso());
    return new Date(d.getFullYear(), d.getMonth(), 1);
  });

  useEffect(() => {
    function onMouseDown(e) {
      if (!datePickerOpen) return;
      if (containerRef.current?.contains(e.target)) return;
      setDatePickerOpen(false);
    }
    document.addEventListener('mousedown', onMouseDown);
    return () => document.removeEventListener('mousedown', onMouseDown);
  }, [datePickerOpen]);

  const selectedDate = parseDisplayDate(value, dateFormat) || parseIsoDate(todayIso());
  const monthYearOptions = useMemo(() => buildMonthYearOptions(pickerMonth, 10), [pickerMonth]);
  const monthYearValue = `${pickerMonth.getFullYear()}-${pickerMonth.getMonth()}`;
  const pickerWeeks = useMemo(() => buildCalendarWeeks(pickerMonth), [pickerMonth]);
  const weekdayLabels = useMemo(() => buildWeekdayLabels(), []);

  function handleInputKeyDown(e) {
    onKeyDown?.(e);
    if (e.defaultPrevented) return;
    if (e.key === 'Tab') {
      setDatePickerOpen(false);
      if (e.shiftKey && onTabPrevious) {
        e.preventDefault();
        onTabPrevious(e);
      } else if (!e.shiftKey && onTabNext) {
        e.preventDefault();
        onTabNext(e);
      }
    }
  }

  function handleInputFocus(e) {
    e.target.select();
    onFocus?.(e);
  }

  function handleInputMouseUp(e) {
    // Keep full selection when focus is gained via mouse click.
    e.preventDefault();
  }

  function handleInputBlur(e) {
    const normalized = normalizeDateInput(e.target.value, value, dateFormat);
    if (normalized !== e.target.value) {
      onChange?.(normalized);
    }
    onBlur?.(e);
  }

  function openDatePicker() {
    if (disabled) return;
    const base = parseDisplayDate(value, dateFormat) || parseIsoDate(todayIso());
    setPickerMonth(new Date(base.getFullYear(), base.getMonth(), 1));
    setDatePickerOpen(v => !v);
  }

  function selectDateFromPicker(day) {
    const selected = new Date(pickerMonth.getFullYear(), pickerMonth.getMonth(), day);
    onChange?.(formatDateForDisplay(selected, dateFormat));
    setDatePickerOpen(false);
    requestAnimationFrame(() => {
      if (ref && typeof ref !== 'function' && ref.current) {
        ref.current.focus();
        ref.current.select();
      }
    });
  }

  function shiftPickerMonth(delta) {
    setPickerMonth(prev => new Date(prev.getFullYear(), prev.getMonth() + delta, 1));
  }

  function handleMonthYearChange(e) {
    const [yearStr, monthStr] = e.target.value.split('-');
    const y = Number.parseInt(yearStr, 10);
    const m = Number.parseInt(monthStr, 10);
    if (Number.isFinite(y) && Number.isFinite(m)) {
      setPickerMonth(new Date(y, m, 1));
    }
  }

  return (
    <div ref={containerRef} className={cn('relative', className)}>
      <Input
        ref={ref}
        type="text"
        inputMode="numeric"
        disabled={disabled}
        className={cn('w-full pr-6', inputClassName)}
        value={value}
        onChange={e => onChange?.(e.target.value)}
        onKeyDown={handleInputKeyDown}
        onFocus={handleInputFocus}
        onMouseUp={handleInputMouseUp}
        onBlur={handleInputBlur}
        placeholder={getDatePlaceholder(dateFormat)}
        aria-label={ariaLabel || t('DATE', 'Date')}
      />
      <button
        type="button"
        disabled={disabled}
        tabIndex={-1}
        onMouseDown={e => e.preventDefault()}
        onClick={openDatePicker}
        className="absolute right-1 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700 disabled:text-gray-300"
        aria-label={ariaLabel || t('DATE', 'Date')}
        title={title || t('DATE', 'Date')}
      >
        <Calendar size={12} />
      </button>
      {datePickerOpen && (
        <div className="absolute left-0 top-full mt-1 z-30 w-56 bg-white border border-gray-300 rounded shadow-lg p-2">
          <div className="flex items-center gap-1 mb-2">
            <button
              type="button"
              className="h-6 w-6 rounded border border-gray-300 text-gray-600 hover:bg-gray-100 flex items-center justify-center"
              onMouseDown={e => e.preventDefault()}
              onClick={() => shiftPickerMonth(-1)}
              tabIndex={-1}
              aria-label={t('PREVIOUS_MONTH', 'Previous month')}
            >
              <ChevronLeft size={12} />
            </button>
            <select
              className="flex-1 h-6 border border-gray-300 rounded text-xs px-1 bg-white text-gray-700"
              value={monthYearValue}
              onChange={handleMonthYearChange}
              tabIndex={-1}
            >
              {monthYearOptions.map(opt => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
            <button
              type="button"
              className="h-6 w-6 rounded border border-gray-300 text-gray-600 hover:bg-gray-100 flex items-center justify-center"
              onMouseDown={e => e.preventDefault()}
              onClick={() => shiftPickerMonth(1)}
              tabIndex={-1}
              aria-label={t('NEXT_MONTH', 'Next month')}
            >
              <ChevronRight size={12} />
            </button>
          </div>
          <div className="grid grid-cols-7 gap-0.5 text-[10px] text-center text-gray-500 mb-1">
            {weekdayLabels.map(d => (
              <div key={d} className="py-0.5">{d}</div>
            ))}
          </div>
          <div className="grid grid-cols-7 gap-0.5 text-xs">
            {pickerWeeks.flat().map((cell, idx) => {
              if (!cell) return <div key={`blank-${idx}`} className="h-6" />;
              const isSelected =
                cell.getFullYear() === selectedDate.getFullYear() &&
                cell.getMonth() === selectedDate.getMonth() &&
                cell.getDate() === selectedDate.getDate();
              return (
                <button
                  key={toIsoDate(cell)}
                  type="button"
                  className={`h-6 rounded ${isSelected ? 'bg-blue-600 text-white' : 'hover:bg-blue-50 text-gray-700'}`}
                  onMouseDown={e => e.preventDefault()}
                  onClick={() => selectDateFromPicker(cell.getDate())}
                  tabIndex={-1}
                >
                  {cell.getDate()}
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
});

function buildMonthYearOptions(centerMonth, yearsEachSide = 10) {
  const options = [];
  const startYear = centerMonth.getFullYear() - yearsEachSide;
  const endYear = centerMonth.getFullYear() + yearsEachSide;
  for (let y = startYear; y <= endYear; y++) {
    for (let m = 0; m < 12; m++) {
      const value = `${y}-${m}`;
      const label = new Date(y, m, 1).toLocaleDateString(undefined, { month: 'long', year: 'numeric' });
      options.push({ value, label });
    }
  }
  return options;
}

function buildCalendarWeeks(monthDate) {
  const year = monthDate.getFullYear();
  const month = monthDate.getMonth();
  const firstDayOfWeek = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const cells = [];
  for (let i = 0; i < firstDayOfWeek; i++) cells.push(null);
  for (let day = 1; day <= daysInMonth; day++) {
    cells.push(new Date(year, month, day));
  }
  while (cells.length % 7 !== 0) cells.push(null);

  const weeks = [];
  for (let i = 0; i < cells.length; i += 7) {
    weeks.push(cells.slice(i, i + 7));
  }
  return weeks;
}

function buildWeekdayLabels() {
  const formatter = new Intl.DateTimeFormat(undefined, { weekday: 'short' });
  const baseSunday = new Date(2024, 0, 7);
  return Array.from({ length: 7 }, (_, i) => formatter.format(new Date(baseSunday.getFullYear(), baseSunday.getMonth(), baseSunday.getDate() + i)));
}
