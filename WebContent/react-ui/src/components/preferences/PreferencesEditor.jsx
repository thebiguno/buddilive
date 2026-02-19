import { useState, useEffect } from 'react';
import { Dialog, DialogContent, DialogFooter } from '../ui/Dialog';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Select } from '../ui/Select';
import { api } from '../../lib/api';
import { useApp } from '../../context/AppContext';

export function PreferencesEditor({ open, onClose }) {
  const { showError } = useApp();
  const [data, setData] = useState(null);
  const [encrypt, setEncrypt] = useState(false);
  const [password, setPassword] = useState('');
  const [useTwoFactor, setUseTwoFactor] = useState(false);
  const [storeEmail, setStoreEmail] = useState(false);
  const [locale, setLocale] = useState('');
  const [currency, setCurrency] = useState('');
  const [dateFormat, setDateFormat] = useState('');
  const [showDeleted, setShowDeleted] = useState(false);
  const [locales, setLocales] = useState([]);
  const [currencies, setCurrencies] = useState([]);
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!open) return;
    setLoading(true);
    Promise.all([
      api.preferences.get(),
      api.get('stores/locales').catch(() => ({ data: [] })),
      api.get('stores/currencies').catch(() => ({ data: [] })),
    ]).then(([prefs, loc, cur]) => {
      setData(prefs);
      setEncrypt(prefs.encrypt || false);
      setUseTwoFactor(prefs.useTwoFactor || false);
      setStoreEmail(prefs.storeEmail || false);
      setLocale(prefs.locale || '');
      setCurrency(prefs.currency || '');
      setDateFormat(prefs.dateFormat || '');
      setShowDeleted(prefs.showDeleted || false);
      setLocales(loc?.data || []);
      setCurrencies(cur?.data || []);
    }).catch(showError).finally(() => setLoading(false));
  }, [open, showError]);

  async function handleSave() {
    setSaving(true);
    try {
      await api.preferences.save({
        action: 'update',
        encrypt,
        encryptPassword: encrypt !== data?.encrypt ? password : undefined,
        useTwoFactor,
        storeEmail,
        locale,
        currency,
        dateFormat,
        showDeleted,
      });
      onClose();
      window.location.reload();
    } catch (e) {
      showError(e);
    } finally {
      setSaving(false);
    }
  }

  const today = new Date();
  const dateFormats = [
    { value: '', text: 'Use Locale Defaults' },
    { value: 'yyyy-MM-dd', text: today.toISOString().split('T')[0] },
    { value: 'MM/dd/yyyy', text: `${String(today.getMonth()+1).padStart(2,'0')}/${String(today.getDate()).padStart(2,'0')}/${today.getFullYear()}` },
    { value: 'dd/MM/yyyy', text: `${String(today.getDate()).padStart(2,'0')}/${String(today.getMonth()+1).padStart(2,'0')}/${today.getFullYear()}` },
  ];

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent title="Preferences" className="w-[480px]" onOk={handleSave} onCancel={onClose}>
        {loading ? (
          <div className="p-4 text-sm text-gray-400">Loading...</div>
        ) : (
          <div className="p-3 flex flex-col gap-2">
            <FormRow label="">
              <label className="flex items-center gap-2 text-xs cursor-pointer">
                <input type="checkbox" checked={encrypt} onChange={e => setEncrypt(e.target.checked)} />
                Encrypt Data
              </label>
            </FormRow>
            {encrypt !== data?.encrypt && (
              <FormRow label="Password">
                <Input type="password" className="flex-1" value={password} onChange={e => setPassword(e.target.value)} />
              </FormRow>
            )}
            <FormRow label="">
              <label className="flex items-center gap-2 text-xs cursor-pointer">
                <input type="checkbox" checked={useTwoFactor} onChange={e => setUseTwoFactor(e.target.checked)} />
                Use Two-Factor Authentication
              </label>
            </FormRow>
            <FormRow label="">
              <label className="flex items-center gap-2 text-xs cursor-pointer">
                <input type="checkbox" checked={storeEmail} onChange={e => setStoreEmail(e.target.checked)} />
                Store Email Address
              </label>
            </FormRow>
            <FormRow label="Locale">
              <Select className="flex-1" value={locale} onChange={e => setLocale(e.target.value)}>
                <option value="">Default</option>
                {locales.map((l, i) => l.value === '' ? <option key={`sep-${i}`} disabled>───────────</option> : <option key={l.value} value={l.value}>{l.text}</option>)}
              </Select>
            </FormRow>
            <FormRow label="Currency">
              <Select className="flex-1" value={currency} onChange={e => setCurrency(e.target.value)}>
                <option value="">Default</option>
                {currencies.map((c, i) => c.value === '' ? <option key={`sep-${i}`} disabled>───────────</option> : <option key={c.value} value={c.value}>{c.text}</option>)}
              </Select>
            </FormRow>
            <FormRow label="Date Format">
              <Select className="flex-1" value={dateFormat} onChange={e => setDateFormat(e.target.value)}>
                {dateFormats.map(f => <option key={f.value} value={f.value}>{f.text}</option>)}
              </Select>
            </FormRow>
            <FormRow label="">
              <label className="flex items-center gap-2 text-xs cursor-pointer">
                <input type="checkbox" checked={showDeleted} onChange={e => setShowDeleted(e.target.checked)} />
                Show Deleted Items
              </label>
            </FormRow>
          </div>
        )}
        <DialogFooter>
          <Button variant="default" onClick={onClose}>Cancel</Button>
          <Button variant="primary" disabled={saving || loading} onClick={handleSave}>OK</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function FormRow({ label, children }) {
  return (
    <div className="flex items-center gap-2">
      <label className="text-xs text-gray-700 w-28 flex-shrink-0">{label}</label>
      <div className="flex-1">{children}</div>
    </div>
  );
}
