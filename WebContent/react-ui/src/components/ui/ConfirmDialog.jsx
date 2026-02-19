import { Dialog, DialogContent, DialogFooter } from './Dialog';
import { Button } from './Button';
import { useApp } from '../../context/AppContext';

export function ConfirmDialog({ open, title, message, onConfirm, onCancel }) {
  const { t } = useApp();

  return (
    <Dialog open={open} onOpenChange={v => !v && onCancel()}>
      <DialogContent title={title} className="w-96" onOk={onConfirm} onCancel={onCancel}>
        <div className="p-4 text-sm">{message}</div>
        <DialogFooter>
          <Button variant="default" onClick={onCancel}>{t('CANCEL', 'Cancel')}</Button>
          <Button variant="primary" onClick={onConfirm}>{t('YES', 'Yes')}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export function AlertDialog({ open, title, message, onClose }) {
  const { t } = useApp();

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent title={title || t('MESSAGE', 'Message')} className="w-96" zIndex={200} onOk={onClose}>
        <div className="p-4 text-sm">{message}</div>
        <DialogFooter>
          <Button variant="primary" onClick={onClose}>{t('OK', 'OK')}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
