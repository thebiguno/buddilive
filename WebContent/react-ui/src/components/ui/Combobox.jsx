import { useState, useRef, useEffect } from 'react';
import { ChevronDown } from 'lucide-react';
import { cn } from '../../lib/utils';

/**
 * Autocomplete combobox with styled dropdown items.
 * options: [{ value, text, style }]
 */
export function Combobox({ options = [], value, onChange, onSelect, placeholder, className, disabled }) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');
  const [activeIndex, setActiveIndex] = useState(-1);
  const containerRef = useRef(null);
  const inputRef = useRef(null);
  const listRef = useRef(null);

  const selected = options.find(o => o.value === value);

  useEffect(() => {
    if (!open) { setQuery(''); setActiveIndex(-1); }
  }, [open]);

  useEffect(() => {
    function handleMouseDown(e) {
      const inContainer = containerRef.current?.contains(e.target);
      const inList = listRef.current?.contains(e.target) || !!e.target.closest?.('[data-combobox-portal]');
      if (!inContainer && !inList) setOpen(false);
    }
    document.addEventListener('mousedown', handleMouseDown);
    return () => document.removeEventListener('mousedown', handleMouseDown);
  }, []);


  const filtered = query
    ? options.filter(o => o.text && o.text.toLowerCase().includes(query.toLowerCase()))
    : options;

  // Only selectable items (non-header entries)
  const selectableFiltered = filtered.filter(o => o.value !== '' && o.value !== null);

  function handleInputChange(e) {
    setQuery(e.target.value);
    setOpen(true);
    setActiveIndex(-1);
    if (onChange) onChange(e.target.value);
  }

  function handleSelect(opt) {
    setOpen(false);
    setQuery('');
    setActiveIndex(-1);
    if (onSelect) onSelect(opt);
  }

  function scrollActiveIntoView(idx) {
    if (!listRef.current) return;
    const items = listRef.current.querySelectorAll('[data-selectable]');
    if (items[idx]) items[idx].scrollIntoView({ block: 'nearest' });
  }

  function handleKeyDown(e) {
    if (e.key === 'Escape') { setOpen(false); setQuery(''); return; }
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      if (!open) { setOpen(true); return; }
      setActiveIndex(i => {
        const next = Math.min(i + 1, selectableFiltered.length - 1);
        scrollActiveIntoView(next);
        return next;
      });
      return;
    }
    if (e.key === 'ArrowUp') {
      e.preventDefault();
      setActiveIndex(i => {
        const next = Math.max(i - 1, 0);
        scrollActiveIntoView(next);
        return next;
      });
      return;
    }
    if (e.key === 'Enter') {
      e.preventDefault();
      if (open && activeIndex >= 0 && selectableFiltered[activeIndex]) {
        handleSelect(selectableFiltered[activeIndex]);
      }
      return;
    }
  }

  return (
    <div ref={containerRef} className={cn('relative', className)}>
      <input
        ref={inputRef}
        type="text"
        className={cn(
          'w-full border border-gray-400 rounded pl-1.5 pr-5 py-0.5 text-xs bg-white',
          'focus:outline-none focus:ring-1 focus:ring-blue-400 focus:border-blue-400',
          'disabled:bg-gray-100 disabled:text-gray-500'
        )}
        value={open ? query : (selected ? selected.text : (value || ''))}
        onChange={handleInputChange}
        onFocus={() => setOpen(true)}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        disabled={disabled}
      />
      <button
        type="button"
        tabIndex={-1}
        disabled={disabled}
        onMouseDown={e => { e.preventDefault(); if (!disabled) { setOpen(o => !o); inputRef.current?.focus(); } }}
        className="absolute right-0 top-0 bottom-0 w-5 flex items-center justify-center text-gray-400 hover:text-gray-600 disabled:opacity-40"
      >
        <ChevronDown size={11} />
      </button>
      {open && filtered.length > 0 && (
        <div
          ref={listRef}
          className="absolute z-[9999] left-0 top-full mt-0.5 min-w-[200px] max-h-60 overflow-y-auto bg-white border border-gray-400 shadow-lg rounded text-xs"
          onMouseDown={e => e.stopPropagation()}
        >
          {(() => {
            let selectableIdx = -1;
            return filtered.map((opt, i) => {
              if (opt.value === '' || opt.value === null) {
                return <div key={i} className="px-2 py-0.5 text-gray-400 border-t border-gray-200 text-[10px] font-semibold uppercase">{opt.text}</div>;
              }
              selectableIdx++;
              const si = selectableIdx;
              const isActive = si === activeIndex;
              return (
                <div
                  key={opt.value ?? i}
                  data-selectable="true"
                  className={cn(
                    'px-2 py-1 cursor-pointer hover:bg-blue-100',
                    isActive ? 'bg-blue-200' : (opt.value === value ? 'bg-blue-50' : '')
                  )}
                  onMouseDown={() => handleSelect(opt)}
                >
                  <span style={opt.style ? parseStyle(opt.style) : undefined}>{opt.text}</span>
                </div>
              );
            });
          })()}
        </div>
      )}
    </div>
  );
}

function parseStyle(styleStr) {
  if (!styleStr) return {};
  const result = {};
  styleStr.split(';').forEach(part => {
    const [key, val] = part.split(':');
    if (key && val) {
      const camel = key.trim().replace(/-([a-z])/g, (_, c) => c.toUpperCase());
      result[camel] = val.trim();
    }
  });
  return result;
}
