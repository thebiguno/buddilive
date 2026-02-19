import { useApp } from '../context/AppContext';

export function useI18n() {
  const { t } = useApp();
  return t;
}
