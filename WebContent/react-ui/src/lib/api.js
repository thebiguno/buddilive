const BASE = '';

async function request(url, options = {}) {
  const res = await fetch(BASE + url, {
    headers: {
      'Accept': 'application/json',
      ...(options.body && !(options.body instanceof FormData) ? { 'Content-Type': 'application/json' } : {}),
      ...options.headers,
    },
    ...options,
  });
  if (!res.ok) {
    let msg = res.statusText || 'Request failed';
    try {
      const j = await res.json();
      if (j && j.msg) msg = j.msg;
    } catch (_) {}
    const err = new Error(msg);
    err.status = res.status;
    throw err;
  }
  const text = await res.text();
  if (!text) return null;
  try { return JSON.parse(text); } catch (_) { return text; }
}

export const api = {
  get: (url) => request(url),
  post: (url, data) => request(url, { method: 'POST', body: data instanceof FormData ? data : JSON.stringify(data) }),

  accounts: {
    list: () => request('data/accounts.json'),
    save: (data) => request('data/accounts', { method: 'POST', body: JSON.stringify(data) }),
  },
  transactions: {
    list: (params = '') => request(`data/transactions.json${params}`),
    descriptions: () => request('data/transactions/descriptions.json'),
    save: (data) => request('data/transactions', { method: 'POST', body: JSON.stringify(data) }),
    splitSources: () => Promise.all([
      request('data/sources/from'),
      request('data/sources/to'),
    ]).then(([from, to]) => ({ from: from?.data || [], to: to?.data || [] })),
  },
  categories: {
    list: (periodType) => request(`data/categories.json?periodType=${periodType}`),
    parents: (excludeId) => request(`data/categories/parents.json${excludeId ? `?exclude=${excludeId}` : ''}`),
    save: (data) => request('data/categories', { method: 'POST', body: JSON.stringify(data) }),
  },
  scheduled: {
    list: () => request('data/scheduledtransactions.json'),
    save: (data) => request('data/scheduledtransactions', { method: 'POST', body: JSON.stringify(data) }),
    execute: (date) => request('data/scheduledtransactions/execute', { method: 'POST', body: JSON.stringify(date) }),
  },
  preferences: {
    get: () => request('data/userpreferences'),
    save: (data) => request('data/userpreferences', { method: 'POST', body: JSON.stringify(data) }),
  },
  backup: () => { window.open('data/backup.json'); },
  exportCsv: (query) => { window.open(`data/export.json?type=CSV&${query}`); },
  reports: {
    pie: (type, query) => request(`data/report/pietotalsbycategory.json?type=${type}&${query}`),
    incomeExpenses: (query) => request(`data/report/incomeandexpensesbycategory.json?${query}`),
    averageIncomeExpenses: (query) => request(`data/report/averageincomeandexpensesbycategory.json?${query}`),
    inflowByAccount: (query) => request(`data/report/inflowandoutflowbyaccount.json?${query}`),
    inflowByPayee: (query) => request(`data/report/inflowandoutflowbypayee.json?${query}`),
    balancesOverTime: (query) => request(`data/report/balancesovertime.json?${query}`),
    netWorth: (query) => request(`data/report/balancesovertime.json?netWorthOnly=true&${query}`),
    budgetVsActual: (query) => request(`data/report/budgetvsactual.json?${query}`),
    monthlyCashFlow: (query) => request(`data/report/monthlycashflow.json?${query}`),
    savingsRate: (query) => request(`data/report/savingsrate.json?${query}`),
    yearOverYear: (query) => request(`data/report/yearoveryear.json?${query}`),
    topPayeesBySpend: (query) => request(`data/report/toppayeesbyspend.json?${query}`),
    categoryDrillDown: (categoryId, query) => request(`data/report/categorydrilldown.json?categoryId=${encodeURIComponent(categoryId)}&${query}`),
    projectedBalance: (days) => request(`data/report/projectedbalance.json?days=${days}`),
    debtPaydown: (query) => request(`data/report/debtpaydown.json?${query}`),
  },
};
