import { useState, useRef, useEffect } from 'react';
import { ChevronDown } from 'lucide-react';
import { cn } from '../../lib/utils';

export function Toolbar({ children, className }) {
  return (
    <div className={cn(
      'flex items-center gap-1 px-2 py-1 bg-gradient-to-b from-[#e8e8e8] to-[#d0d0d0] border-b border-gray-400 flex-shrink-0',
      className
    )}>
      {children}
    </div>
  );
}

export function ToolbarSeparator() {
  return <div className="w-px h-5 bg-gray-400 mx-1" />;
}

export function ToolbarSpacer() {
  return <div className="flex-1" />;
}

export function ToolbarButton({ icon, label, disabled, onClick, className }) {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      className={cn(
        'inline-flex items-center gap-1 px-2 py-1 rounded border border-transparent text-xs',
        'hover:bg-white/60 hover:border-gray-400 active:bg-gray-200',
        'disabled:opacity-40 disabled:cursor-not-allowed',
        'transition-colors cursor-pointer',
        className
      )}
    >
      {icon && <span className="flex-shrink-0">{icon}</span>}
      {label && <span>{label}</span>}
    </button>
  );
}

export function ToolbarMenu({ icon, label, items, disabled }) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    function handler(e) {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    }
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  return (
    <div ref={ref} className="relative">
      <button
        onClick={() => !disabled && setOpen(o => !o)}
        disabled={disabled}
        className={cn(
          'inline-flex items-center gap-1 px-2 py-1 rounded border border-transparent text-xs',
          'hover:bg-white/60 hover:border-gray-400 active:bg-gray-200',
          'disabled:opacity-40 disabled:cursor-not-allowed transition-colors cursor-pointer',
          open && 'bg-white/60 border-gray-400'
        )}
      >
        {icon && <span>{icon}</span>}
        {label && <span>{label}</span>}
        <ChevronDown size={10} />
      </button>
      {open && (
        <div className="absolute right-0 top-full mt-0.5 z-50 bg-white border border-gray-300 shadow-lg rounded min-w-[220px] py-1">
          {items.map((item, i) => {
            if (item === '-' || item.separator) {
              return <div key={i} className="border-t border-gray-200 my-1" />;
            }
            if (item.header) {
              return (
                <div key={i} className="px-3 pt-1.5 pb-0.5 text-[10px] font-semibold tracking-wider text-gray-400 uppercase select-none">
                  {item.label}
                </div>
              );
            }
            return (
              <button
                key={i}
                disabled={item.disabled}
                title={item.tooltip}
                onClick={() => { setOpen(false); item.onClick && item.onClick(); }}
                className={cn(
                  'w-full text-left flex items-center gap-2 px-3 py-1.5 text-xs',
                  'hover:bg-blue-50 disabled:opacity-40 disabled:cursor-not-allowed',
                  'cursor-pointer'
                )}
              >
                {item.icon && <span className="w-4 flex-shrink-0">{item.icon}</span>}
                <span>{item.label}</span>
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
