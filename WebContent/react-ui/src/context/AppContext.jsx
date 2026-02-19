import React, { createContext, useContext, useState, useCallback } from 'react';

const AppContext = createContext(null);

export function AppProvider({ children, userConfig }) {
  const [error, setError] = useState(null);
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

  return (
    <AppContext.Provider value={{
      userConfig,
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
