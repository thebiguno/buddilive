import * as DialogPrimitive from '@radix-ui/react-dialog';
import { X } from 'lucide-react';
import { cn } from '../../lib/utils';

export function Dialog({ open, onOpenChange, children }) {
  return (
    <DialogPrimitive.Root open={open} onOpenChange={onOpenChange}>
      {children}
    </DialogPrimitive.Root>
  );
}

export function DialogContent({ className, title, children, onOk, onCancel, zIndex, ...props }) {
  function handleKeyDown(e) {
    if (e.key === 'Enter' && !e.defaultPrevented) {
      const target = e.target;
      if (target.tagName === 'TEXTAREA') return;
      if (target.tagName === 'BUTTON') return;
      if (target.tagName === 'SELECT') return;
      if (onOk) { e.preventDefault(); onOk(); }
    }
  }
  return (
    <DialogPrimitive.Portal>
      <DialogPrimitive.Overlay className="fixed inset-0 bg-black/40 z-50" style={zIndex ? { zIndex } : undefined} />
      <DialogPrimitive.Content
        className={cn(
          'fixed left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 z-50',
          'bg-white border border-gray-300 shadow-xl rounded',
          'flex flex-col',
          className
        )}
        style={zIndex ? { zIndex } : undefined}
        onPointerDownOutside={e => e.preventDefault()}
        onInteractOutside={e => e.preventDefault()}
        onOpenAutoFocus={e => { if (!e.target.querySelector('[autofocus]')) e.preventDefault(); }}
        onKeyDown={handleKeyDown}
        {...props}
      >
        {title && (
          <div className="flex items-center justify-between px-3 py-2 bg-gradient-to-b from-[#d8d8d8] to-[#b8b8b8] border-b border-gray-300 rounded-t">
            <DialogPrimitive.Title className="font-semibold text-sm text-gray-800">{title}</DialogPrimitive.Title>
            <DialogPrimitive.Close className="text-gray-500 hover:text-gray-800 ml-4">
              <X size={14} />
            </DialogPrimitive.Close>
          </div>
        )}
        <div className="flex-1">
          {children}
        </div>
      </DialogPrimitive.Content>
    </DialogPrimitive.Portal>
  );
}

export function DialogFooter({ children }) {
  return (
    <div className="flex justify-end gap-2 px-3 py-2 bg-[#f0f0f0] border-t border-gray-300 rounded-b">
      {children}
    </div>
  );
}
