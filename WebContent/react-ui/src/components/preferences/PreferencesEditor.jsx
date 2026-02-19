import { useState, useEffect } from 'react';
import { Dialog, DialogContent, DialogFooter } from '../ui/Dialog';
import { ConfirmDialog } from '../ui/ConfirmDialog';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Select } from '../ui/Select';
import { HelpCircle } from 'lucide-react';
import { api } from '../../lib/api';
import { useApp } from '../../context/AppContext';

const PREFERENCE_HELP = {
  encrypt: {
    titleKey: 'ENCRYPT_DATA',
    bodyKey: 'HELP_ENCRYPT_DATA',
  },
  encryptPassword: {
    titleKey: 'CURRENT_PASSWORD',
    bodyKey: 'HELP_ENCRYPT_DATA_PASSWORD',
  },
  twoFactor: {
    titleKey: 'USE_TWO_FACTOR',
    bodyKey: 'HELP_USE_TWO_FACTOR',
  },
  regenerateTwoFactorBackup: {
    titleKey: 'REGENERATE_TWO_FACTOR_BACKUP',
    bodyKey: 'HELP_REGENERATE_TWO_FACTOR_BACKUP',
  },
  storeEmail: {
    titleKey: 'STORE_EMAIL',
    bodyKey: 'HELP_STORE_EMAIL',
  },
  locale: {
    titleKey: 'LOCALE',
    bodyKey: 'HELP_LOCALE',
  },
  currency: {
    titleKey: 'CURRENCY',
    bodyKey: 'HELP_CURRENCY',
  },
  dateFormat: {
    titleKey: 'DATE_FORMAT',
    bodyKey: 'HELP_DATE_FORMAT',
  },
  showDeleted: {
    titleKey: 'SHOW_DELETED',
    bodyKey: 'HELP_SHOW_DELETED',
  },
};

export function PreferencesEditor({ open, onClose }) {
  const { showError, t } = useApp();
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
  const [disableTwoFactorPending, setDisableTwoFactorPending] = useState(false);
  const [disableTwoFactorPassword, setDisableTwoFactorPassword] = useState('');
  const [disableEncryptPending, setDisableEncryptPending] = useState(false);
  const [disableEncryptPassword, setDisableEncryptPassword] = useState('');
  const [regeneratingTwoFactorBackup, setRegeneratingTwoFactorBackup] = useState(false);
  const [confirmRegenerateTwoFactorBackup, setConfirmRegenerateTwoFactorBackup] = useState(false);
  const [helpKey, setHelpKey] = useState(null);

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
    const encryptPassword = encrypt !== data?.encrypt
      ? (data?.encrypt && !encrypt ? disableEncryptPassword : password)
      : undefined;

    setSaving(true);
    try {
      await api.preferences.save({
        action: 'update',
        encrypt,
        encryptPassword,
        useTwoFactor,
        disableTwoFactorPassword: data?.useTwoFactor && !useTwoFactor ? disableTwoFactorPassword : undefined,
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

  async function handleRegenerateTwoFactorBackup() {
    setRegeneratingTwoFactorBackup(true);
    try {
      await api.preferences.save({ action: 'invalidatetotpbackups' });
      window.location.reload();
    } catch (e) {
      showError(e);
    } finally {
      setRegeneratingTwoFactorBackup(false);
    }
  }

  const today = new Date();
  const dateFormats = [
    { value: '', text: t('USE_LOCALE_DEFAULTS', 'Use Locale Defaults') },
    { value: 'yyyy-MM-dd', text: today.toISOString().split('T')[0] },
    { value: 'MM/dd/yyyy', text: `${String(today.getMonth()+1).padStart(2,'0')}/${String(today.getDate()).padStart(2,'0')}/${today.getFullYear()}` },
    { value: 'dd/MM/yyyy', text: `${String(today.getDate()).padStart(2,'0')}/${String(today.getMonth()+1).padStart(2,'0')}/${today.getFullYear()}` },
  ];

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent title={t('PREFERENCES', 'Preferences')} className="w-[480px]" onOk={handleSave} onCancel={onClose}>
        {loading ? (
          <div className="p-4 text-sm text-gray-400">{t('LOADING', 'Loading...')}</div>
        ) : (
          <div className="p-3 flex flex-col gap-2">
            <FormRow label="" onHelp={() => setHelpKey('encrypt')}>
              <label className="flex items-center gap-2 text-xs cursor-pointer">
                <input type="checkbox" checked={encrypt} onChange={e => {
                  if (data?.encrypt && !e.target.checked) {
                    setEncrypt(false);
                    setDisableEncryptPending(true);
                    setDisableEncryptPassword('');
                  } else {
                    setEncrypt(e.target.checked);
                    if (e.target.checked) {
                      setDisableEncryptPending(false);
                      setDisableEncryptPassword('');
                    } else {
                      setPassword('');
                    }
                  }
                }} />
                {t('ENCRYPT_DATA', 'Encrypt Data')}
              </label>
            </FormRow>
            {disableEncryptPending && (
              <div className="border border-amber-300 bg-amber-50 rounded p-3 flex flex-col gap-2 text-xs">
                <p className="text-amber-800 font-semibold">⚠ {t('WARNING_DISABLE_ENCRYPTION_TITLE', 'Disable Encryption?')}</p>
                <p className="text-amber-700">{t('WARNING_DISABLE_ENCRYPTION_BODY', 'This will decrypt all your data. Your data will be stored unencrypted on the server.')}</p>
                <FormRow label={t('CURRENT_PASSWORD', 'Current Password')} onHelp={() => setHelpKey('encryptPassword')}>
                  <Input type="password" className="flex-1" autoFocus value={disableEncryptPassword} onChange={e => setDisableEncryptPassword(e.target.value)} placeholder={t('PLACEHOLDER_ENTER_CURRENT_PASSWORD_CONFIRM', 'Enter current password to confirm')} />
                </FormRow>
              </div>
            )}
            {!data?.encrypt && encrypt && (
              <div className="border border-amber-300 bg-amber-50 rounded p-3 flex flex-col gap-2 text-xs">
                <p className="text-amber-800 font-semibold">⚠ {t('WARNING_ENABLE_ENCRYPTION_TITLE', 'Enable Encryption?')}</p>
                <p className="text-amber-700">{t('WARNING_ENABLE_ENCRYPTION_BODY', 'Your login password will be used to encrypt your data. Once enabled, you will no longer be able to reset your password via the "Forgot Password" link - there is no backdoor to recover encrypted data.')}</p>
                <FormRow label={t('CURRENT_PASSWORD', 'Current Password')} onHelp={() => setHelpKey('encryptPassword')}>
                  <Input type="password" className="flex-1" autoFocus value={password} onChange={e => setPassword(e.target.value)} placeholder={t('PLACEHOLDER_ENTER_LOGIN_PASSWORD', 'Enter your login password')} />
                </FormRow>
              </div>
            )}
            <FormRow label="" onHelp={() => setHelpKey('twoFactor')}>
              <label className="flex items-center gap-2 text-xs cursor-pointer">
                <input type="checkbox" checked={useTwoFactor} onChange={e => {
                  if (data?.useTwoFactor && !e.target.checked) {
                    setUseTwoFactor(false);
                    setDisableTwoFactorPending(true);
                    setDisableTwoFactorPassword('');
                  } else {
                    setUseTwoFactor(e.target.checked);
                    if (e.target.checked) {
                      setDisableTwoFactorPending(false);
                      setDisableTwoFactorPassword('');
                    }
                  }
                }} />
                {t('USE_TWO_FACTOR', 'Use Two-Factor Authentication')}
              </label>
            </FormRow>
            {disableTwoFactorPending && (
              <div className="border border-amber-300 bg-amber-50 rounded p-3 flex flex-col gap-2 text-xs">
                <p className="text-amber-800 font-semibold">⚠ {t('WARNING_DISABLE_TWO_FACTOR_TITLE', 'Disable Two-Factor Authentication?')}</p>
                <p className="text-amber-700">{t('WARNING_DISABLE_TWO_FACTOR_BODY', 'This will permanently remove your 2FA secret. You will need to set up a new authenticator app if you want to re-enable it.')}</p>
                <FormRow label={t('CURRENT_PASSWORD', 'Current Password')}>
                  <Input type="password" className="flex-1" autoFocus value={disableTwoFactorPassword} onChange={e => setDisableTwoFactorPassword(e.target.value)} placeholder={t('PLACEHOLDER_ENTER_CURRENT_PASSWORD_CONFIRM', 'Enter current password to confirm')} />
                </FormRow>
              </div>
            )}
            {data?.useTwoFactor && (
              <FormRow label="" onHelp={() => setHelpKey('regenerateTwoFactorBackup')}>
                <Button
                  variant="default"
                  disabled={loading || saving || regeneratingTwoFactorBackup}
                  onClick={() => setConfirmRegenerateTwoFactorBackup(true)}
                >
                  {t('REGENERATE_TWO_FACTOR_BACKUP', 'Regenerate Two-Factor Backup Codes')}
                </Button>
              </FormRow>
            )}
            {!data?.useTwoFactor && useTwoFactor && (
              <div className="border border-amber-300 bg-amber-50 rounded p-3 flex flex-col gap-2 text-xs">
                <p className="text-amber-800 font-semibold">⚠ {t('WARNING_ENABLE_TWO_FACTOR_TITLE', 'Set Up Two-Factor Authentication')}</p>
                <p className="text-amber-700">{t('WARNING_ENABLE_TWO_FACTOR_BODY', 'After you click OK, you will be guided through setup with an authenticator app (such as Google Authenticator or Authy). Keep your phone with you so you can scan a QR code and finish setup.')}</p>
              </div>
            )}
            <FormRow label="" onHelp={() => setHelpKey('storeEmail')}>
              <label className="flex items-center gap-2 text-xs cursor-pointer">
                <input type="checkbox" checked={storeEmail} onChange={e => setStoreEmail(e.target.checked)} />
                {t('STORE_EMAIL', 'Store Email')}
              </label>
            </FormRow>
            {!data?.storeEmail && storeEmail && (
              <div className="border border-amber-300 bg-amber-50 rounded p-3 flex flex-col gap-2 text-xs">
                <p className="text-amber-800 font-semibold">⚠ {t('WARNING_STORE_EMAIL_TITLE', 'Store Email Address?')}</p>
                <p className="text-amber-700">{t('WARNING_STORE_EMAIL_BODY', 'Enabling this stores your login email address in a recoverable form so Buddi Live can send system emails. If you prefer maximum privacy and anonymity, leave this disabled.')}</p>
              </div>
            )}
            <FormRow label={t('LOCALE', 'Locale')} onHelp={() => setHelpKey('locale')}>
              <Select className="flex-1" value={locale} onChange={e => setLocale(e.target.value)}>
                <option value="">{t('DEFAULT', 'Default')}</option>
                {locales.map((l, i) => l.value === '' ? <option key={`sep-${i}`} disabled>───────────</option> : <option key={l.value} value={l.value}>{l.text}</option>)}
              </Select>
            </FormRow>
            <FormRow label={t('CURRENCY', 'Currency')} onHelp={() => setHelpKey('currency')}>
              <Select className="flex-1" value={currency} onChange={e => setCurrency(e.target.value)}>
                <option value="">{t('DEFAULT', 'Default')}</option>
                {currencies.map((c, i) => c.value === '' ? <option key={`sep-${i}`} disabled>───────────</option> : <option key={c.value} value={c.value}>{c.text}</option>)}
              </Select>
            </FormRow>
            <FormRow label={t('DATE_FORMAT', 'Date Format')} onHelp={() => setHelpKey('dateFormat')}>
              <Select className="flex-1" value={dateFormat} onChange={e => setDateFormat(e.target.value)}>
                {dateFormats.map(f => <option key={f.value} value={f.value}>{f.text}</option>)}
              </Select>
            </FormRow>
            <FormRow label="" onHelp={() => setHelpKey('showDeleted')}>
              <label className="flex items-center gap-2 text-xs cursor-pointer">
                <input type="checkbox" checked={showDeleted} onChange={e => setShowDeleted(e.target.checked)} />
                {t('SHOW_DELETED', 'Show Deleted Accounts and Budget Categories')}
              </label>
            </FormRow>
          </div>
        )}
        <DialogFooter>
          <Button variant="default" onClick={onClose}>{t('CANCEL', 'Cancel')}</Button>
          <Button
            variant="primary"
            disabled={
              saving
              || loading
              || (data?.encrypt && !encrypt && !disableEncryptPassword.trim())
              || (!data?.encrypt && encrypt && !password.trim())
              || (data?.useTwoFactor && !useTwoFactor && !disableTwoFactorPassword.trim())
            }
            onClick={handleSave}
          >
            {t('OK', 'OK')}
          </Button>
        </DialogFooter>
      </DialogContent>

      <Dialog open={!!helpKey} onOpenChange={v => !v && setHelpKey(null)}>
        <DialogContent
          title={t(PREFERENCE_HELP[helpKey]?.titleKey, t('HELP', 'Help'))}
          className="w-[560px]"
          onOk={() => setHelpKey(null)}
          onCancel={() => setHelpKey(null)}
          zIndex={200}
        >
          <div
            className="p-4 text-xs text-gray-700 leading-relaxed"
            dangerouslySetInnerHTML={{ __html: t(PREFERENCE_HELP[helpKey]?.bodyKey, '') }}
          />
          <DialogFooter>
            <Button variant="primary" onClick={() => setHelpKey(null)}>{t('OK', 'OK')}</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {confirmRegenerateTwoFactorBackup && (
        <ConfirmDialog
          open={true}
          title={t('CONFIRM_REGENERATE_TWO_FACTOR_BACKUP_TITLE', 'Regenerate Two-Factor Backup Codes?')}
          message={t('CONFIRM_REGENERATE_TWO_FACTOR_BACKUP_MESSAGE', 'This will permanently invalidate all existing backup codes and generate a new set. Continue?')}
          onConfirm={async () => {
            setConfirmRegenerateTwoFactorBackup(false);
            await handleRegenerateTwoFactorBackup();
          }}
          onCancel={() => setConfirmRegenerateTwoFactorBackup(false)}
        />
      )}
    </Dialog>
  );
}

function FormRow({ label, children, onHelp }) {
  const { t } = useApp();
  const helpTitle = t('HELP', 'Help');

  return (
    <div className="flex items-center gap-2">
      <label className="text-xs text-gray-700 w-28 flex-shrink-0">{label}</label>
      <div className="flex-1">{children}</div>
      {onHelp && (
        <button
          type="button"
          onClick={onHelp}
          className="text-gray-500 hover:text-gray-800 cursor-pointer"
          title={helpTitle}
        >
          <HelpCircle size={14} />
        </button>
      )}
    </div>
  );
}
