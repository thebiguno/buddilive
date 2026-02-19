import { useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';

export function ContextMenu({ x, y, items, onClose }) {
  const ref = useRef(null);

  useEffect(() => {
    function onDown(e) {
      if (ref.current && !ref.current.contains(e.target)) onClose();
    }
    document.addEventListener('mousedown', onDown);
    return () => document.removeEventListener('mousedown', onDown);
  }, [onClose]);

  // Clamp to viewport
  const style = { position: 'fixed', top: y, left: x, zIndex: 9999 };

  return createPortal(
    <div
      ref={ref}
      style={style}
      className="bg-white border border-gray-300 rounded shadow-lg py-0.5 min-w-[140px] text-xs"
    >
      {items.map((item, i) =>
        item === '-' ? (
          <div key={i} className="border-t border-gray-200 my-0.5" />
        ) : (
          <button
            key={i}
            disabled={item.disabled}
            className="w-full text-left px-3 py-1.5 hover:bg-blue-500 hover:text-white disabled:text-gray-400 disabled:hover:bg-transparent disabled:hover:text-gray-400 cursor-default"
            onMouseDown={e => { e.preventDefault(); }}
            onClick={() => { onClose(); item.onClick(); }}
          >
            {item.label}
          </button>
        )
      )}
    </div>,
    document.body
  );
}
