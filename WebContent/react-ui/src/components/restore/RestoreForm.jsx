import { useState, useRef } from 'react';
import { Dialog, DialogContent, DialogFooter } from '../ui/Dialog';
import { Button } from '../ui/Button';
import { useApp } from '../../context/AppContext';

export function RestoreForm({ open, onClose }) {
  const { showError, t } = useApp();
  const [file, setFile] = useState(null);
  const [deleteData, setDeleteData] = useState(false);
  const [saving, setSaving] = useState(false);
  const fileRef = useRef(null);

  async function handleSave() {
    if (!file) return;
    setSaving(true);
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('deleteData', deleteData ? 'true' : 'false');
      const res = await fetch('data/restore', {
        method: 'POST',
        body: formData,
      });
      if (!res.ok) {
        const j = await res.json().catch(() => ({}));
        throw new Error(j.msg || res.statusText);
      }
      onClose();
      window.location.reload();
    } catch (e) {
      showError(e);
    } finally {
      setSaving(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent title={t('RESTORE', 'Restore')} className="w-96" onOk={handleSave} onCancel={onClose}>
        <div className="p-3 flex flex-col gap-3">
          <div className="flex items-center gap-2">
            <label className="text-xs text-gray-700 w-28 flex-shrink-0">{t('RESTORE_FILE', 'Restore File')}</label>
            <input
              ref={fileRef}
              type="file"
              className="hidden"
              onChange={e => setFile(e.target.files[0] || null)}
            />
            <button
              type="button"
              className="px-2 py-1 text-xs border border-gray-300 rounded bg-white hover:bg-gray-50 active:bg-gray-100 cursor-pointer"
              onClick={() => fileRef.current?.click()}
            >
              {t('CHOOSE_FILE', 'Choose File')}
            </button>
            <span className="text-xs text-gray-500 truncate">{file ? file.name : t('NO_FILE_CHOSEN', 'No file chosen')}</span>
          </div>
          <div className="flex items-center gap-2">
            <label className="text-xs text-gray-700 w-28 flex-shrink-0" />
            <label className="flex items-center gap-2 text-xs cursor-pointer">
              <input
                type="checkbox"
                checked={deleteData}
                onChange={e => setDeleteData(e.target.checked)}
              />
              {t('DELETE_DATA_BEFORE_RESTORE', 'Delete existing data before restore')}
            </label>
          </div>
        </div>
        <DialogFooter>
          <Button variant="default" onClick={onClose}>{t('CANCEL', 'Cancel')}</Button>
          <Button variant="primary" disabled={!file || saving} onClick={handleSave}>
            {saving ? t('RESTORING', 'Restoring...') : t('OK', 'OK')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
