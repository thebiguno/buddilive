import { useState, useRef } from 'react';
import { Dialog, DialogContent, DialogFooter } from '../ui/Dialog';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { api } from '../../lib/api';
import { useApp } from '../../context/AppContext';

const PROBLEM_LABEL_KEYS = {
  length:     'PASSWORD_LENGTH',
  strength:   'PASSWORD_STRENGTH',
  variance:   'PASSWORD_VARIANCE',
  classes:    'PASSWORD_CLASSES',
  history:    'PASSWORD_HISTORY',
  dictionary: 'PASSWORD_DICTIONARY',
  pattern:    'PASSWORD_PATTERN',
  custom:     'PASSWORD_CUSTOM',
};

export function ChangePasswordEditor({ open, onClose }) {
  const { showError, userConfig, t } = useApp();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [saving, setSaving] = useState(false);
  const [checkResult, setCheckResult] = useState(null);
  const [score, setScore] = useState(0);
  const timer = useRef(null);

  const passwordsMatch = newPassword === confirmPassword;
  const passed = checkResult?.passed === true;
  const isValid = currentPassword.length > 0 && passed && passwordsMatch;

  function scoreToBar(s) {
    const pct = Math.min(s || 0, 100);
    const color = pct < 20 ? '#953131' : pct < 40 ? '#b2894f' : pct < 60 ? '#bc9c45' : pct < 80 ? '#8cac4a' : '#26a826';
    return { pct, color };
  }

  function checkPassword(pw, co) {
    if (!pw) { setCheckResult(null); setScore(0); return; }
    fetch('authentication/checkPassword', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: 'identifier=anonymous&secret=' + encodeURIComponent(pw),
    })
      .then(r => r.json())
      .then(d => { setCheckResult(d); setScore(d?.score || 0); })
      .catch(() => {});
  }

  function handleNewPasswordChange(e) {
    const v = e.target.value;
    setNewPassword(v);
    clearTimeout(timer.current);
    timer.current = setTimeout(() => checkPassword(v, confirmPassword), 300);
  }

  const problems = [];
  if (checkResult && !checkResult.passed) {
    Object.entries(PROBLEM_LABEL_KEYS).forEach(([key, i18nKey]) => {
      if (checkResult[key] === false) problems.push(t(i18nKey, i18nKey));
    });
  }
  if (confirmPassword.length > 0 && !passwordsMatch) problems.push(t('PASSWORD_CONFIRMATION_MATCH', 'Passwords must match'));

  async function handleSave() {
    setSaving(true);
    try {
      await api.preferences.save({
        action: 'changePassword',
        currentPassword,
        newPassword,
      });
      const identifier = userConfig?.identifier;
      if (identifier) {
        await fetch('authentication/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: 'identifier=' + encodeURIComponent(identifier) + '&password=' + encodeURIComponent(newPassword),
        });
      }
      onClose();
    } catch (e) {
      showError(e);
    } finally {
      setSaving(false);
    }
  }

  const bar = newPassword.length > 0 ? scoreToBar(score) : null;

  return (
    <Dialog open={open} onOpenChange={v => !v && onClose()}>
      <DialogContent title={t('CHANGE_PASSWORD', 'Change Password')} className="w-[480px]" onOk={handleSave} onCancel={onClose}>
        <div className="p-3 flex flex-col gap-2">
          <FormRow label={t('CURRENT_PASSWORD', 'Current Password')}>
            <Input
              type="password"
              className="flex-1"
              value={currentPassword}
              onChange={e => setCurrentPassword(e.target.value)}
              autoFocus
            />
          </FormRow>
          {/* Two side-by-side inputs matching the register widget */}
          <FormRow label={t('NEW_PASSWORD', 'New Password')}>
            <div className="flex gap-1.5 flex-1">
              <Input
                type="password"
                className="flex-1"
                value={newPassword}
                onChange={handleNewPasswordChange}
                placeholder={t('PASSWORD_LABEL', 'Password')}
              />
              <Input
                type="password"
                className="flex-1"
                value={confirmPassword}
                onChange={e => setConfirmPassword(e.target.value)}
                placeholder={`${t('CONFIRM', 'Confirm')} ✓`}
              />
            </div>
          </FormRow>
          {/* Strength bar — indented to align with inputs, colour from server score */}
          {bar && (
            <div className="ml-[8.5rem] h-1.5 bg-gray-200 rounded-full overflow-hidden">
              <div
                className="h-full rounded-full transition-all"
                style={{ width: bar.pct + '%', backgroundColor: bar.color }}
              />
            </div>
          )}
          {/* Problems list */}
          {newPassword.length > 0 && problems.length > 0 && (
            <div className="ml-[8.5rem] text-xs text-red-600">
              <span className="font-bold">{t('PASSWORD_PROBLEMS', 'Problems')}:</span>
              {problems.map(p => (
                <div key={p}>{p}</div>
              ))}
            </div>
          )}
        </div>
        <DialogFooter>
          <Button variant="default" onClick={onClose}>{t('CANCEL', 'Cancel')}</Button>
          <Button variant="primary" disabled={!isValid || saving} onClick={handleSave}>{t('OK', 'OK')}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function FormRow({ label, children }) {
  return (
    <div className="flex items-center gap-2">
      <label className="text-xs text-gray-700 w-32 flex-shrink-0">{label}</label>
      {children}
    </div>
  );
}
