import { forwardRef, useState, useRef, useEffect } from 'react';
import { ChevronDown } from 'lucide-react';
import { cn } from '../../lib/utils';

/**
 * Autocomplete combobox with styled dropdown items.
 * options: [{ value, text, style }]
 */
export const Combobox = forwardRef(function Combobox({ options = [], value, onChange, onSelect, placeholder, className, disabled, filterOnFocus = false }, forwardedRef) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');
  const [activeIndex, setActiveIndex] = useState(-1);
  const containerRef = useRef(null);
  const inputRef = useRef(null);
  const listRef = useRef(null);
  const typedSinceLastSelectRef = useRef(false);

  const selected = options.find(o => o.value === value);
  const displayValue = selected ? selected.text : (value ?? '');

  function getSelectableIndexByValue(list, selectedValue) {
    let selectableIdx = -1;
    let found = -1;
    for (const opt of list) {
      if (opt.value === '' || opt.value === null) continue;
      selectableIdx++;
      if (opt.value === selectedValue) {
        found = selectableIdx;
        break;
      }
    }
    return found;
  }

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

  function setInputRef(node) {
    inputRef.current = node;
    if (!forwardedRef) return;
    if (typeof forwardedRef === 'function') forwardedRef(node);
    else forwardedRef.current = node;
  }

  const normalizedQuery = normalizeForSearch(query);
  const filtered = normalizedQuery
    ? options.filter(o => normalizeForSearch(o.text).includes(normalizedQuery))
    : options;

  // Only selectable items (non-header entries)
  const selectableFiltered = filtered.filter(o => o.value !== '' && o.value !== null);

  function handleInputChange(e) {
    typedSinceLastSelectRef.current = true;
    setQuery(e.target.value);
    setOpen(true);
    setActiveIndex(-1);
    if (onChange) onChange(e.target.value);
  }

  function handleSelect(opt) {
    typedSinceLastSelectRef.current = false;
    setOpen(false);
    setQuery('');
    setActiveIndex(-1);
    if (onSelect) onSelect(opt);
  }

  function openAndHighlightCurrent() {
    const shouldFilterOnFocus = !!(
      filterOnFocus &&
      typedSinceLastSelectRef.current &&
      String(displayValue || '').trim().length > 0
    );
    const nextQuery = shouldFilterOnFocus ? String(displayValue) : '';
    const normalizedFocusQuery = normalizeForSearch(nextQuery);
    const filteredOnFocus = normalizedFocusQuery
      ? options.filter(o => normalizeForSearch(o.text).includes(normalizedFocusQuery))
      : options;
    const selectableOnFocus = filteredOnFocus.filter(o => o.value !== '' && o.value !== null);
    const selectedIndex = getSelectableIndexByValue(filteredOnFocus, value);
    const nextIndex = selectedIndex >= 0 ? selectedIndex : (shouldFilterOnFocus && selectableOnFocus.length > 0 ? 0 : -1);
    setQuery(nextQuery);
    setOpen(true);
    setActiveIndex(nextIndex);
    if (nextIndex >= 0) {
      requestAnimationFrame(() => scrollActiveIntoView(nextIndex));
    }
  }

  function scrollActiveIntoView(idx) {
    if (!listRef.current) return;
    const items = listRef.current.querySelectorAll('[data-selectable]');
    if (items[idx]) items[idx].scrollIntoView({ block: 'nearest' });
  }

  function handleKeyDown(e) {
    if (e.key === 'Escape') { setOpen(false); setQuery(''); return; }
    if (e.key === 'Tab') {
      // Let browser tab navigation proceed, but close the list immediately.
      setOpen(false);
      setQuery('');
      setActiveIndex(-1);
      return;
    }
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      if (!open) { openAndHighlightCurrent(); return; }
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
      if (open) {
        const option = activeIndex >= 0
          ? selectableFiltered[activeIndex]
          : selectableFiltered[0];
        if (option) {
          handleSelect(option);
        }
      }
      return;
    }
  }

  function handleInputBlur() {
    // Always collapse the picklist when the textbox loses focus.
    setOpen(false);
    setQuery('');
    setActiveIndex(-1);
  }

  return (
    <div ref={containerRef} className={cn('relative', className)}>
      <input
        ref={setInputRef}
        type="text"
        className={cn(
          'w-full border border-gray-400 rounded pl-1.5 pr-5 py-0.5 text-xs bg-white',
          'focus:outline-none focus:ring-1 focus:ring-blue-400 focus:border-blue-400',
          'disabled:bg-gray-100 disabled:text-gray-500'
        )}
        value={open ? (query !== '' ? query : displayValue) : displayValue}
        onChange={handleInputChange}
        onFocus={openAndHighlightCurrent}
        onBlur={handleInputBlur}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        disabled={disabled}
      />
      <button
        type="button"
        tabIndex={-1}
        disabled={disabled}
        onMouseDown={e => {
          e.preventDefault();
          if (disabled) return;
          if (open) setOpen(false);
          else openAndHighlightCurrent();
          inputRef.current?.focus();
        }}
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
});

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

function normalizeForSearch(value) {
  return String(value || '')
    .toLowerCase()
    .replace(/[^a-z0-9]/g, '');
}
