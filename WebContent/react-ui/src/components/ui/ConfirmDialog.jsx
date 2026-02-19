import { Dialog, DialogContent, DialogFooter } from './Dialog';
import { Button } from './Button';

export function ConfirmDialog({ open, title, message, onConfirm, onCancel }) {
  return (
    <Dialog open={open} onOpenChange={v => !v && onCancel()}>
      <DialogContent title={title} className="w-96" onOk={onConfirm} onCancel={onCancel}>
        <div className="p-4 text-sm">{message}</div>
        <DialogFooter>
          <Button variant="default" onClick={onCancel}>Cancel</Button>
          <Button variant="primary" onClick={onConfirm}>Yes</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export function AlertDialog({ open, title, message, onClose }) {
  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent title={title || 'Message'} className="w-96" zIndex={200} onOk={onClose}>
        <div className="p-4 text-sm">{message}</div>
        <DialogFooter>
          <Button variant="primary" onClick={onClose}>OK</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
