import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';

const AppContext = createContext(null);

export function AppProvider({ children, userConfig }) {
  const [error, setError] = useState(null);
  const [i18n, setI18n] = useState(userConfig?.i18n || {});
  const [splitSources, setSplitSources] = useState({ from: [], to: [] });
  const [accountTreeVersion, setAccountTreeVersion] = useState(0);
  const [transactionListVersion, setTransactionListVersion] = useState(0);
  const [descriptionStoreVersion, setDescriptionStoreVersion] = useState(0);

  const showError = useCallback((err) => {
    const msg = err instanceof Error ? err.message : String(err);
    setError(msg);
  }, []);

  const clearError = useCallback(() => setError(null), []);

  const refreshAccounts = useCallback(() => setAccountTreeVersion(v => v + 1), []);
  const refreshTransactions = useCallback(() => setTransactionListVersion(v => v + 1), []);
  const refreshDescriptions = useCallback(() => setDescriptionStoreVersion(v => v + 1), []);
  const t = useCallback((key, fallback) => i18n?.[key] || fallback || key, [i18n]);

  useEffect(() => {
    let cancelled = false;
    fetch('stores/translations', { headers: { Accept: 'application/json' } })
      .then(r => (r.ok ? r.json() : null))
      .then(json => {
        if (!cancelled && json && typeof json === 'object') {
          const next = { ...json };
          delete next.success;
          setI18n(prev => ({ ...prev, ...next }));
        }
      })
      .catch(() => {});

    return () => { cancelled = true; };
  }, []);

  return (
    <AppContext.Provider value={{
      userConfig,
      i18n, t,
      error, showError, clearError,
      splitSources, setSplitSources,
      accountTreeVersion, refreshAccounts,
      transactionListVersion, refreshTransactions,
      descriptionStoreVersion, refreshDescriptions,
    }}>
      {children}
    </AppContext.Provider>
  );
}

export function useApp() {
  return useContext(AppContext);
}
