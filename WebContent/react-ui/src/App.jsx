import { useState, useEffect, useCallback, useRef } from 'react';
import { AppProvider, useApp } from './context/AppContext';
import { TabBar } from './components/ui/Tabs';
import { Toolbar, ToolbarButton, ToolbarMenu, ToolbarSeparator, ToolbarSpacer } from './components/ui/Toolbar';
import { AlertDialog, ConfirmDialog } from './components/ui/ConfirmDialog';
import { AccountTree } from './components/accounts/AccountTree';
import { AccountEditor } from './components/accounts/AccountEditor';
import { TransactionList } from './components/transactions/TransactionList';
import { TransactionEditor } from './components/transactions/TransactionEditor';
import { BudgetTree } from './components/budget/BudgetTree';
import { BudgetEditor } from './components/budget/BudgetEditor';
import { ScheduledList } from './components/scheduled/ScheduledList';
import { ScheduledEditor } from './components/scheduled/ScheduledEditor';
import { PreferencesEditor } from './components/preferences/PreferencesEditor';
import { ChangePasswordEditor } from './components/preferences/ChangePasswordEditor';
import { RestoreForm } from './components/restore/RestoreForm';
import { IntervalPicker } from './components/reports/IntervalPicker';
import { PieReport } from './components/reports/PieReport';
import { BalancesOverTimeReport, NetWorthOverTimeReport } from './components/reports/LineReport';
import { IncomeExpensesReport, AverageIncomeExpensesReport, InflowByAccountReport, InflowByPayeeReport } from './components/reports/TableReport';
import { BudgetVsActualReport, MonthlyCashFlowReport, SavingsRateReport, YearOverYearReport } from './components/reports/BarReport';
import { TopPayeesBySpendReport, CategoryDrillDownReport, ProjectedBalanceReport, DebtPaydownReport, CategoryPickerDialog, ProjectedBalancePickerDialog } from './components/reports/AdvancedReports';
import { ReportInfoButton } from './components/reports/ReportInfo';
import { Input } from './components/ui/Input';
import { api } from './lib/api';
import {
  BarChart2, PlusCircle, Edit2, Trash2, RefreshCw,
  Lock, Award, LogOut, Settings, Clock, Download,
  Upload, FileText, HelpCircle, DollarSign, UserX, Key,
  Table2, PieChart, TrendingUp, LineChart
} from 'lucide-react';

// Tab IDs for built-in tabs
const TAB_ACCOUNTS = 'accounts';
const TAB_BUDGET = 'budget';

// Budget period types
const BUDGET_PERIODS = [
  { value: 'WEEK', key: 'PERIOD_WEEKLY', text: 'Weekly' },
  { value: 'SEMI_MONTH', key: 'PERIOD_SEMI_MONTHLY', text: 'Semi-Monthly' },
  { value: 'MONTH', key: 'PERIOD_MONTHLY', text: 'Monthly' },
  { value: 'QUARTER', key: 'PERIOD_QUARTERLY', text: 'Quarterly' },
  { value: 'SEMI_YEAR', key: 'PERIOD_SEMI_YEARLY', text: 'Semi-Yearly' },
  { value: 'YEAR', key: 'PERIOD_YEARLY', text: 'Yearly' },
];

function AccountsTabLayout({ selectedAccount, setSelectedAccount, setSelectedTransaction, selectedTransaction, refreshTransactions, refreshAccounts, refreshDescriptions, setConfirmDialog, showError, onAdd, onEdit, onDelete }) {
  const { t } = useApp();
  const [sidebarWidth, setSidebarWidth] = useState(288);
  const dragging = useRef(false);
  const startX = useRef(0);
  const startWidth = useRef(0);

  function onDividerMouseDown(e) {
    dragging.current = true;
    startX.current = e.clientX;
    startWidth.current = sidebarWidth;
    document.body.style.cursor = 'col-resize';
    document.body.style.userSelect = 'none';

    function onMouseMove(e) {
      if (!dragging.current) return;
      const delta = e.clientX - startX.current;
      setSidebarWidth(Math.max(150, Math.min(600, startWidth.current + delta)));
    }
    function onMouseUp() {
      dragging.current = false;
      document.body.style.cursor = '';
      document.body.style.userSelect = '';
      document.removeEventListener('mousemove', onMouseMove);
      document.removeEventListener('mouseup', onMouseUp);
    }
    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', onMouseUp);
  }

  return (
    <div className="flex h-full">
      <div style={{ width: sidebarWidth }} className="flex-shrink-0 h-full">
        <AccountTree
          selectedAccount={selectedAccount}
          onAccountSelect={acc => {
            setSelectedAccount(acc);
            setSelectedTransaction(null);
          }}
          onAdd={onAdd}
          onEdit={onEdit}
          onDelete={onDelete}
        />
      </div>
      <div
        className="w-1 flex-shrink-0 h-full bg-gray-300 hover:bg-blue-400 cursor-col-resize active:bg-blue-500 transition-colors"
        onMouseDown={onDividerMouseDown}
      />
      <div className="flex-1 flex flex-col h-full min-w-0 relative">
        {!selectedAccount && (
          <div className="absolute inset-0 bg-white/70 z-10 flex items-center justify-center pointer-events-all select-none">
            <span className="text-sm text-gray-400">{t('SELECT_ACCOUNT_TO_VIEW_TRANSACTIONS', 'Select an account to view transactions')}</span>
          </div>
        )}
        <TransactionEditor
          selectedAccount={selectedAccount}
          selectedTransaction={selectedTransaction}
          onSaved={() => { refreshTransactions(); refreshAccounts(); refreshDescriptions(); setSelectedTransaction(null); }}
          onClear={() => setSelectedTransaction(null)}
          onDelete={id => {
            setConfirmDialog({
              title: t('DELETE_TRANSACTION_TITLE', 'Delete Transaction'),
              message: t('CONFIRM_DELETE_TRANSACTION_SIMPLE', 'Delete this transaction?'),
              onConfirm: async () => {
                try {
                  await api.transactions.save({ action: 'delete', id });
                  refreshTransactions(); refreshAccounts(); refreshDescriptions();
                  setSelectedTransaction(null);
                } catch (e) { showError(e); }
                setConfirmDialog(null);
              },
              onCancel: () => setConfirmDialog(null),
            });
          }}
        />
        <div className="flex-1 min-h-0">
          <TransactionList
            selectedAccount={selectedAccount}
            selectedTransactionId={selectedTransaction?.id}
            onTransactionSelect={t => setSelectedTransaction(t)}
          />
        </div>
      </div>
    </div>
  );
}

function BuddiApp({ userConfig }) {
  const {
    t,
    showError, error, clearError,
    refreshAccounts, refreshTransactions, refreshDescriptions,
    accountTreeVersion,
  } = useApp();

  // Tab state
  const [tabs, setTabs] = useState([
    { id: TAB_ACCOUNTS, label: t('MY_ACCOUNTS', 'My Accounts'), closable: false },
    { id: TAB_BUDGET, label: t('MY_BUDGET', 'My Budget'), closable: false },
  ]);
  const [activeTab, setActiveTab] = useState(TAB_ACCOUNTS);

  // Keep non-closable root tab labels in sync once i18n translations load.
  useEffect(() => {
    setTabs(prev => prev.map(tab => {
      if (tab.id === TAB_ACCOUNTS) return { ...tab, label: t('MY_ACCOUNTS', 'My Accounts') };
      if (tab.id === TAB_BUDGET) return { ...tab, label: t('MY_BUDGET', 'My Budget') };
      return tab;
    }));
  }, [t]);

  // Accounts panel state
  const [selectedAccount, setSelectedAccount] = useState(null);
  const [selectedTransaction, setSelectedTransaction] = useState(null);

  // Budget panel state
  const [activeBudgetPeriod, setActiveBudgetPeriod] = useState('MONTH');
  const [selectedBudgetNode, setSelectedBudgetNode] = useState(null);
  const [budgetTreeVersion, setBudgetTreeVersion] = useState(0);

  // Scheduled panel state
  const [selectedScheduled, setSelectedScheduled] = useState(null);

  // Dialog state
  const [accountEditorOpen, setAccountEditorOpen] = useState(false);
  const [accountEditorSelected, setAccountEditorSelected] = useState(null);
  const [budgetEditorOpen, setBudgetEditorOpen] = useState(false);
  const [budgetEditorSelected, setBudgetEditorSelected] = useState(null);
  const [scheduledEditorOpen, setScheduledEditorOpen] = useState(false);
  const [scheduledEditorSelected, setScheduledEditorSelected] = useState(null);
  const [preferencesOpen, setPreferencesOpen] = useState(false);
  const [changePasswordOpen, setChangePasswordOpen] = useState(false);
  const [restoreOpen, setRestoreOpen] = useState(false);
  const [intervalPickerOpen, setIntervalPickerOpen] = useState(false);
  const [intervalPickerCallback, setIntervalPickerCallback] = useState(null);
  const [categoryPickerCallback, setCategoryPickerCallback] = useState(null);
  const [projectedPickerOpen, setProjectedPickerOpen] = useState(false);
  const [confirmDialog, setConfirmDialog] = useState(null);
  const [alertDialog, setAlertDialog] = useState(null);
  const [deleteAccountConfirmText, setDeleteAccountConfirmText] = useState('');
  const [deletingAccount, setDeletingAccount] = useState(false);

  const [budgetPeriodTabs, setBudgetPeriodTabs] = useState(BUDGET_PERIODS);

  useEffect(() => {
    Promise.all(
      BUDGET_PERIODS.map(p =>
        api.get(`data/categories.json?periodType=${p.value}`)
          .then(d => ({ ...p, hasData: (d?.children?.length ?? 0) > 0 }))
          .catch(() => ({ ...p, hasData: false }))
      )
    ).then(results => {
      const active = results.filter(p => p.hasData);
      if (active.length > 0) {
        setBudgetPeriodTabs(active);
        if (!active.find(p => p.value === activeBudgetPeriod)) {
          setActiveBudgetPeriod(active[0].value);
        }
      }
    });
  }, [budgetTreeVersion]);

  // Load account tree nodes for line chart
  const [accountTreeNodes, setAccountTreeNodes] = useState([]);
  useEffect(() => {
    api.accounts.list().then(d => setAccountTreeNodes(d?.children || [])).catch(() => {});
  }, [accountTreeVersion]);

  // Hourly keepalive: execute scheduled transactions (mirrors old ExtJS Application.js behaviour)
  const runScheduled = useCallback(async () => {
    try {
      const today = new Date().toISOString().slice(0, 10);
      const result = await api.scheduled.execute(today);
      if (result?.messages?.length > 0) {
        refreshAccounts();
        refreshTransactions();
        refreshDescriptions();
      }
    } catch (_) {}
  }, [refreshAccounts, refreshTransactions, refreshDescriptions]);

  useEffect(() => {
    runScheduled();
    const id = setInterval(runScheduled, 60 * 60 * 1000);
    return () => clearInterval(id);
  }, [runScheduled]);

  useEffect(() => {
    const id = setInterval(() => { api.preferences.get().catch(() => {}); }, 5 * 60 * 1000);
    return () => clearInterval(id);
  }, []);


  // ── Tab helpers ──────────────────────────────────────────────────────────

  function openReportTab(id, label, content) {
    setTabs(prev => {
      if (prev.find(t => t.id === id)) return prev;
      return [...prev, { id, label, closable: true, content }];
    });
    setActiveTab(id);
  }

  function closeTab(id) {
    setTabs(prev => prev.filter(t => t.id !== id));
    setActiveTab(prev => prev === id ? TAB_ACCOUNTS : prev);
  }

  function showIntervalPicker(cb) {
    setIntervalPickerCallback(() => cb);
    setIntervalPickerOpen(true);
  }

  // ── Account actions ──────────────────────────────────────────────────────

  function handleAddAccount() {
    setAccountEditorSelected(null);
    setAccountEditorOpen(true);
  }

  function handleEditAccount() {
    if (!selectedAccount) return;
    setAccountEditorSelected(selectedAccount);
    setAccountEditorOpen(true);
  }

  function handleDeleteAccount() {
    if (!selectedAccount) return;
    if (selectedAccount.deleted) {
      setConfirmDialog({
        title: t('RESTORE_ACCOUNT_TITLE', 'Restore Account'),
        message: `${t('RESTORE_ACCOUNT_MESSAGE_PREFIX', 'Restore account')} "${selectedAccount.name}"?`,
        onConfirm: async () => {
          try {
            await api.accounts.save({ action: 'undelete', id: selectedAccount.id });
            refreshAccounts();
            setSelectedAccount(null);
          } catch (e) { showError(e); }
          setConfirmDialog(null);
        },
      });
    } else {
      setConfirmDialog({
        title: t('DELETE_ACCOUNT_TITLE', 'Delete Account'),
        message: `${t('DELETE_ACCOUNT_MESSAGE_PREFIX', 'Delete account')} "${selectedAccount.name}"? ${t('THIS_CANNOT_BE_UNDONE', 'This cannot be undone.')}`,
        onConfirm: async () => {
          try {
            await api.accounts.save({ action: 'delete', id: selectedAccount.id });
            refreshAccounts();
            setSelectedAccount(null);
          } catch (e) { showError(e); }
          setConfirmDialog(null);
        },
      });
    }
  }

  // ── Budget actions ───────────────────────────────────────────────────────

  function handleAddCategory() {
    setBudgetEditorSelected(null);
    setBudgetEditorOpen(true);
  }

  function handleEditCategory() {
    if (!selectedBudgetNode) return;
    setBudgetEditorSelected(selectedBudgetNode);
    setBudgetEditorOpen(true);
  }

  function handleDeleteCategory() {
    if (!selectedBudgetNode) return;
    if (selectedBudgetNode.deleted) {
      setConfirmDialog({
        title: t('RESTORE_CATEGORY_TITLE', 'Restore Category'),
        message: `${t('RESTORE_CATEGORY_MESSAGE_PREFIX', 'Restore category')} "${selectedBudgetNode.name}"?`,
        onConfirm: async () => {
          try {
            await api.categories.save({ action: 'undelete', id: selectedBudgetNode.id });
            setSelectedBudgetNode(null);
          } catch (e) { showError(e); }
          setConfirmDialog(null);
        },
      });
    } else {
      setConfirmDialog({
        title: t('DELETE_CATEGORY_TITLE', 'Delete Category'),
        message: `${t('DELETE_CATEGORY_MESSAGE_PREFIX', 'Delete category')} "${selectedBudgetNode.name}"?`,
        onConfirm: async () => {
          try {
            await api.categories.save({ action: 'delete', id: selectedBudgetNode.id });
            setSelectedBudgetNode(null);
          } catch (e) { showError(e); }
          setConfirmDialog(null);
        },
      });
    }
  }

  // ── Scheduled actions ────────────────────────────────────────────────────

  function handleAddScheduled() {
    setScheduledEditorSelected(null);
    setScheduledEditorOpen(true);
  }

  function handleEditScheduled(row) {
    const target = row || selectedScheduled;
    if (!target) return;
    setScheduledEditorSelected(target);
    setScheduledEditorOpen(true);
  }

  function handleDeleteScheduled(row) {
    const target = row || selectedScheduled;
    if (!target) return;
    setConfirmDialog({
      title: t('DELETE_SCHEDULED_TRANSACTION_TITLE', 'Delete Scheduled Transaction'),
      message: `${t('DELETE_SCHEDULED_MESSAGE_PREFIX', 'Delete scheduled transaction')} "${target.name}"?`,
      onConfirm: async () => {
        try {
          await api.scheduled.save({ action: 'delete', id: target.id });
          setSelectedScheduled(null);
          ScheduledList.reload?.();
        } catch (e) { showError(e); }
        setConfirmDialog(null);
      },
    });
  }

  // ── Delete user ──────────────────────────────────────────────────────────

  function renderDeleteAccountWarning({ requireTypedDelete = false } = {}) {
    return (
      <div className="border border-red-300 bg-red-50 rounded p-3 text-xs flex flex-col gap-2">
        <p className="text-red-800 font-semibold">⚠ {t('WARNING_DELETE_ACCOUNT_TITLE', 'Delete Account Permanently?')}</p>
        <p className="text-red-700">{t('WARNING_DELETE_ACCOUNT_BODY_1', 'This will permanently delete your Buddi Live account and all associated data, including accounts, transactions, budget categories, scheduled transactions, and preferences.')}</p>
        <p className="text-red-700 font-semibold">{t('WARNING_DELETE_ACCOUNT_BODY_2', 'This action cannot be undone. There is no recovery path after deletion.')}</p>
        <p className="text-red-700">{t('WARNING_DELETE_ACCOUNT_BODY_3', 'If you may need this data later, create a backup before continuing.')}</p>
        {requireTypedDelete && (
          <div className="mt-1 pt-2 border-t border-red-300 flex flex-col gap-1.5">
            <p className="text-red-800 font-semibold">{t('WARNING_DELETE_ACCOUNT_TYPE_DELETE', 'Type "delete" to confirm permanent account deletion.')}</p>
            <Input
              className="w-full"
              autoFocus
              value={deleteAccountConfirmText}
              onChange={e => setDeleteAccountConfirmText(e.target.value)}
              placeholder={t('WARNING_DELETE_ACCOUNT_TYPE_DELETE_PLACEHOLDER', 'delete')}
            />
          </div>
        )}
      </div>
    );
  }

  function handleDeleteUser() {
    setDeleteAccountConfirmText('');
    setConfirmDialog({
      title: t('DELETE_ACCOUNT_TITLE', 'Delete Account'),
      message: renderDeleteAccountWarning(),
      onConfirm: () => {
        setDeleteAccountConfirmText('');
        setConfirmDialog({
          title: t('DELETE_ACCOUNT_FINAL_TITLE', 'Delete Account - Final Confirmation'),
          messageType: 'deleteAccountFinalWarning',
          confirmRequiresText: 'delete',
          onConfirm: async () => {
            setConfirmDialog(null);
            setDeletingAccount(true);
            try {
              await api.preferences.save({ action: 'delete' });
              window.location.reload();
            } catch (e) {
              showError(e);
              setDeletingAccount(false);
            }
            setDeleteAccountConfirmText('');
          },
          onCancel: () => {
            setConfirmDialog(null);
            setDeleteAccountConfirmText('');
          },
        });
      },
      onCancel: () => {
        setConfirmDialog(null);
        setDeleteAccountConfirmText('');
      },
    });
  }

  // ── Report launchers ─────────────────────────────────────────────────────

  function launchReport(type, options) {
    const id = `report-${type}-${Date.now()}`;
    const labels = {
      'pie-income': `${t('REPORT_PIE_INCOME_BY_CATEGORY', 'Income by Category')} - ${options.dateRange}`,
      'pie-expenses': `${t('REPORT_PIE_EXPENSES_BY_CATEGORY', 'Expenses by Category')} - ${options.dateRange}`,
      'income-expenses': `${t('REPORT_TABLE_INCOME_AND_EXPENSES_BY_CATEGORY', 'Income and Expenses by Category')} - ${options.dateRange}`,
      'avg-income-expenses': `${t('REPORT_TABLE_AVERAGE_INCOME_AND_EXPENSES_BY_CATEGORY', 'Average Income and Expenses by Category')} - ${options.dateRange}`,
      'inflow-account': `${t('REPORT_TABLE_INFLOW_AND_OUTFLOW_BY_ACCOUNT', 'Inflow and Outflow by Account')} - ${options.dateRange}`,
      'inflow-payee': `${t('REPORT_TABLE_INFLOW_AND_OUTFLOW_BY_PAYEE', 'Inflow and Outflow by Payee')} - ${options.dateRange}`,
      'balances-over-time': `${t('REPORT_ACCOUNT_BALANCES_OVER_TIME', 'Account Balances Over Time')} - ${options.dateRange}`,
      'net-worth': `${t('REPORT_NET_WORTH_OVER_TIME', 'Net Worth Over Time')} - ${options.dateRange}`,
      'budget-vs-actual': `${t('REPORT_BUDGET_VS_ACTUAL', 'Budget vs Actual')} - ${options.dateRange}`,
      'monthly-cash-flow': `${t('REPORT_MONTHLY_CASH_FLOW', 'Monthly Cash Flow')} - ${options.dateRange}`,
      'savings-rate': `${t('REPORT_SAVINGS_RATE', 'Savings Rate')} - ${options.dateRange}`,
      'year-over-year': `${t('REPORT_YEAR_OVER_YEAR', 'Side by Side Period Comparison')} - ${options.dateRange}`,
      'top-payees': `${t('REPORT_TOP_PAYEES_BY_SPEND', 'Top Payees by Spend')} - ${options.dateRange}`,
      'debt-paydown': `${t('REPORT_DEBT_PAYDOWN', 'Debt Paydown')} - ${options.dateRange}`,
      'category-drilldown': `${t('REPORT_CATEGORY_DRILLDOWN', 'Category Drill-Down')} - ${options.categoryName || options.categoryId}`,
      'projected-balance': `${t('REPORT_PROJECTED_BALANCE', 'Projected Balance')} - ${options.days} ${t('DAYS', 'days')}`,
    };
    const label = labels[type];
    if (!label) return;
    openReportTab(id, label, { reportType: type, options });
  }

  // ── Toolbar for each tab type ─────────────────────────────────────────────

  function getToolbarItems() {
    const isAccounts = activeTab === TAB_ACCOUNTS;
    const isBudget = activeTab === TAB_BUDGET;
    const isScheduled = activeTab === 'scheduled';
    const isReport = !isAccounts && !isBudget && !isScheduled;

    const left = [];
    if (isAccounts) {
      left.push(
        <ToolbarButton key="add" icon={<PlusCircle size={13} />} label={t('NEW_ACCOUNT', 'New Account')} onClick={handleAddAccount} />,
        <ToolbarButton key="edit" icon={<Edit2 size={13} />} label={t('MODIFY_ACCOUNT', 'Edit Account')} disabled={!selectedAccount} onClick={handleEditAccount} />,
        <ToolbarButton key="del" icon={<Trash2 size={13} />} label={selectedAccount?.deleted ? t('UNDELETE_ACCOUNT', 'Restore Account') : t('DELETE_ACCOUNT', 'Delete Account')} disabled={!selectedAccount} onClick={handleDeleteAccount} />,
      );
    } else if (isBudget) {
      left.push(
        <ToolbarButton key="add" icon={<PlusCircle size={13} />} label={t('NEW_BUDGET_CATEGORY', 'New Category')} onClick={handleAddCategory} />,
        <ToolbarButton key="edit" icon={<Edit2 size={13} />} label={t('MODIFY_BUDGET_CATEGORY', 'Edit Category')} disabled={!selectedBudgetNode} onClick={handleEditCategory} />,
        <ToolbarButton key="del" icon={<Trash2 size={13} />} label={selectedBudgetNode?.deleted ? t('UNDELETE_BUDGET_CATEGORY', 'Restore Category') : t('DELETE_BUDGET_CATEGORY', 'Delete Category')} disabled={!selectedBudgetNode} onClick={handleDeleteCategory} />,
      );
    } else if (isScheduled) {
      left.push(
        <ToolbarButton key="add" icon={<PlusCircle size={13} />} label={t('NEW_SCHEDULED_TRANSACTION', 'New Scheduled')} onClick={handleAddScheduled} />,
        <ToolbarButton key="edit" icon={<Edit2 size={13} />} label={t('MODIFY_SCHEDULED_TRANSACTION', 'Edit Scheduled')} disabled={!selectedScheduled} onClick={handleEditScheduled} />,
        <ToolbarButton key="del" icon={<Trash2 size={13} />} label={t('DELETE_SCHEDULED_TRANSACTION', 'Delete Scheduled')} disabled={!selectedScheduled} onClick={handleDeleteScheduled} />,
      );
    } else if (isReport) {
      const activeTabObj = tabs.find(t => t.id === activeTab);
      const reportType = activeTabObj?.content?.reportType;
      left.push(
        <ReportInfoButton key="info" reportType={reportType} />,
        <ToolbarButton key="refresh" icon={<RefreshCw size={13} />} label={t('REFRESH', 'Refresh')} onClick={() => {
          // Force re-render of active report tab by bumping its key
          setTabs(prev => prev.map(t => t.id === activeTab ? { ...t, key: (t.key || 0) + 1 } : t));
        }} />,
      );
    }

    return left;
  }

  const reportsMenuItems = [
    { label: t('REPORT_GROUP_BUDGET_SPENDING', 'BUDGET & SPENDING'), header: true },
    { label: t('REPORT_TABLE_INCOME_AND_EXPENSES_BY_CATEGORY', 'Income & Expenses by Category'), icon: <Table2 size={12} />, onClick: () => showIntervalPicker(o => launchReport('income-expenses', o)) },
    { label: t('REPORT_TABLE_AVERAGE_INCOME_AND_EXPENSES_BY_CATEGORY', 'Avg Income & Expenses by Category'), icon: <Table2 size={12} />, onClick: () => showIntervalPicker(o => launchReport('avg-income-expenses', o)) },
    { label: t('REPORT_BUDGET_VS_ACTUAL', 'Budget vs Actual'), icon: <BarChart2 size={12} />, onClick: () => showIntervalPicker(o => launchReport('budget-vs-actual', o)), disabled: !userConfig?.premium, tooltip: !userConfig?.premium ? t('PREMIUM_TOOLTIP', 'Enabled by a donation - thank you!') : undefined },
    { label: t('REPORT_SAVINGS_RATE', 'Savings Rate'), icon: <LineChart size={12} />, onClick: () => showIntervalPicker(o => launchReport('savings-rate', o)), disabled: !userConfig?.premium, tooltip: !userConfig?.premium ? t('PREMIUM_TOOLTIP', 'Enabled by a donation - thank you!') : undefined },
    { label: t('REPORT_CATEGORY_DRILLDOWN', 'Category Drill-Down'), icon: <LineChart size={12} />, onClick: () => setCategoryPickerCallback(() => (cat) => launchReport('category-drilldown', cat)), disabled: !userConfig?.premium, tooltip: !userConfig?.premium ? t('PREMIUM_TOOLTIP', 'Enabled by a donation - thank you!') : undefined },
    { label: t('REPORT_TOP_PAYEES_BY_SPEND', 'Top Payees by Spend'), icon: <BarChart2 size={12} />, onClick: () => showIntervalPicker(o => launchReport('top-payees', o)), disabled: !userConfig?.premium, tooltip: !userConfig?.premium ? t('PREMIUM_TOOLTIP', 'Enabled by a donation - thank you!') : undefined },
    { label: t('REPORT_PIE_INCOME_BY_CATEGORY', 'Income by Category (Pie)'), icon: <PieChart size={12} />, onClick: () => showIntervalPicker(o => launchReport('pie-income', o)) },
    { label: t('REPORT_PIE_EXPENSES_BY_CATEGORY', 'Expenses by Category (Pie)'), icon: <PieChart size={12} />, onClick: () => showIntervalPicker(o => launchReport('pie-expenses', o)) },
    '-',
    { label: t('REPORT_GROUP_ACCOUNTS_CASH_FLOW', 'ACCOUNTS & CASH FLOW'), header: true },
    { label: t('REPORT_TABLE_INFLOW_AND_OUTFLOW_BY_ACCOUNT', 'Inflow & Outflow by Account'), icon: <Table2 size={12} />, onClick: () => showIntervalPicker(o => launchReport('inflow-account', o)), disabled: !userConfig?.premium, tooltip: !userConfig?.premium ? t('PREMIUM_TOOLTIP', 'Enabled by a donation - thank you!') : undefined },
    { label: t('REPORT_TABLE_INFLOW_AND_OUTFLOW_BY_PAYEE', 'Inflow & Outflow by Payee'), icon: <Table2 size={12} />, onClick: () => showIntervalPicker(o => launchReport('inflow-payee', o)), disabled: !userConfig?.premium, tooltip: !userConfig?.premium ? t('PREMIUM_TOOLTIP', 'Enabled by a donation - thank you!') : undefined },
    { label: t('REPORT_MONTHLY_CASH_FLOW', 'Monthly Cash Flow'), icon: <BarChart2 size={12} />, onClick: () => showIntervalPicker(o => launchReport('monthly-cash-flow', o)), disabled: !userConfig?.premium, tooltip: !userConfig?.premium ? t('PREMIUM_TOOLTIP', 'Enabled by a donation - thank you!') : undefined },
    { label: t('REPORT_ACCOUNT_BALANCES_OVER_TIME', 'Account Balances Over Time'), icon: <LineChart size={12} />, onClick: () => showIntervalPicker(o => launchReport('balances-over-time', o)) },
    { label: t('REPORT_NET_WORTH_OVER_TIME', 'Net Worth Over Time'), icon: <TrendingUp size={12} />, onClick: () => showIntervalPicker(o => launchReport('net-worth', o)) },
    { label: t('REPORT_PROJECTED_BALANCE', 'Projected Balance'), icon: <TrendingUp size={12} />, onClick: () => setProjectedPickerOpen(true), disabled: !userConfig?.premium, tooltip: !userConfig?.premium ? t('PREMIUM_TOOLTIP', 'Enabled by a donation - thank you!') : undefined },
    { label: t('REPORT_DEBT_PAYDOWN', 'Debt Paydown Tracker'), icon: <LineChart size={12} />, onClick: () => showIntervalPicker(o => launchReport('debt-paydown', o)), disabled: !userConfig?.premium, tooltip: !userConfig?.premium ? t('PREMIUM_TOOLTIP', 'Enabled by a donation - thank you!') : undefined },
    '-',
    { label: t('REPORT_GROUP_COMPARISON', 'COMPARISON'), header: true },
    { label: t('REPORT_YEAR_OVER_YEAR', 'Side by Side Period Comparison'), icon: <BarChart2 size={12} />, onClick: () => showIntervalPicker(o => launchReport('year-over-year', o)), disabled: !userConfig?.premium, tooltip: !userConfig?.premium ? t('PREMIUM_TOOLTIP', 'Enabled by a donation - thank you!') : undefined },
  ];

  const donateUrl = 'https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=YSF44FWNVSMSN&source=url';

  const systemMenuItems = [
    { label: t('CHANGE_PASSWORD', 'Change Password'), icon: <Key size={12} />, onClick: () => setChangePasswordOpen(true) },
    { label: t('PREFERENCES', 'Preferences'), icon: <Settings size={12} />, onClick: () => setPreferencesOpen(true) },
    { label: t('SCHEDULED_TRANSACTIONS', 'Scheduled Transactions'), icon: <Clock size={12} />, onClick: () => {
      if (!tabs.find(t => t.id === 'scheduled')) {
        setTabs(prev => [...prev, { id: 'scheduled', label: t('SCHEDULED_TRANSACTIONS', 'Scheduled Transactions'), closable: true }]);
      }
      setActiveTab('scheduled');
    }},
    '-',
    { label: t('BACKUP', 'Backup'), icon: <Download size={12} />, onClick: () => api.backup() },
    { label: t('RESTORE', 'Restore'), icon: <Upload size={12} />, onClick: () => setRestoreOpen(true) },
    { label: t('EXPORT_CSV', 'Export CSV'), icon: <FileText size={12} />, disabled: !userConfig?.premium, tooltip: !userConfig?.premium ? t('PREMIUM_TOOLTIP', 'Enabled by a donation - thank you!') : undefined, onClick: () => showIntervalPicker(o => api.exportCsv(o.query)) },
    '-',
    { label: t('HELP_GETTING_STARTED_TITLE', 'Getting Started'), icon: <HelpCircle size={12} />, onClick: () => setAlertDialog({ title: t('HELP_GETTING_STARTED_TITLE', 'Getting Started'), messageHtml: `${t('HELP_GETTING_STARTED_SIMPLE', 'Welcome to Buddi Live! Start by adding accounts in the My Accounts tab, then set up budget categories in My Budget.')}<br/><br/>${t('HELP_GETTING_STARTED_DOCS_PREFIX', 'Need help getting started? You can view ')}<a href="./doc/" target="_blank" rel="noopener noreferrer" style="color:#2563eb;text-decoration:underline;">${t('HELP_GETTING_STARTED_DOCS_LINK', 'tutorials and additional documentation')}</a>${t('HELP_GETTING_STARTED_DOCS_SUFFIX', ' here.')}` }) },
    {
      label: t('DONATE_TITLE', 'Donate'),
      icon: <DollarSign size={12} />,
      onClick: () => setConfirmDialog({
        title: t('DONATE_TITLE', 'Donate'),
        message: `${t('DONATE_DIALOG_MESSAGE_PREFIX', 'I maintain and support Buddi Live in my own time. If you have the means to do so, I would greatly appreciate a donation. A donation of any amount will enable the premium features, including the ability to export transactions and access to more reports. You can donate via ')}${t('DONATE_DIALOG_PAYPAL', 'PayPal')}${t('DONATE_DIALOG_MESSAGE_SUFFIX', '.')}`,
        confirmLabel: t('DONATE_TITLE', 'Donate'),
        onConfirm: () => {
          window.open(donateUrl, '_blank', 'noopener,noreferrer');
          setConfirmDialog(null);
        },
        onCancel: () => setConfirmDialog(null),
      }),
    },
    '-',
    { label: t('DELETE_USER', 'Delete Account'), icon: <UserX size={12} />, onClick: handleDeleteUser },
  ];

  // ── Render active tab content ─────────────────────────────────────────────

  function renderTabContent(tab) {
    if (tab.id === TAB_ACCOUNTS) {
      return <AccountsTabLayout selectedAccount={selectedAccount} setSelectedAccount={setSelectedAccount} setSelectedTransaction={setSelectedTransaction} selectedTransaction={selectedTransaction} refreshTransactions={refreshTransactions} refreshAccounts={refreshAccounts} refreshDescriptions={refreshDescriptions} setConfirmDialog={setConfirmDialog} showError={showError} onAdd={handleAddAccount} onEdit={handleEditAccount} onDelete={handleDeleteAccount} />;
    }

    if (tab.id === TAB_BUDGET) {
      return (
        <div className="flex flex-col h-full">
          {/* Budget period sub-tabs */}
          <div className="flex items-end gap-0 bg-[#d8d8d8] border-b border-gray-400 flex-shrink-0 overflow-x-auto">
            {budgetPeriodTabs.map(p => (
              <div
                key={p.value}
                className={`px-3 py-1.5 text-xs cursor-pointer border-r border-gray-400 select-none hover:bg-[#e8e8e8] ${activeBudgetPeriod === p.value ? 'bg-white font-medium border-t-2 border-t-blue-500 -mb-px pb-2' : 'bg-[#d0d0d0] mt-0.5'}`}
                onClick={() => setActiveBudgetPeriod(p.value)}
              >
                {t(p.key, p.text)}
              </div>
            ))}
          </div>
          <div className="flex-1 min-h-0">
            <BudgetTree
              key={activeBudgetPeriod}
              periodType={activeBudgetPeriod}
              onSelectionChange={setSelectedBudgetNode}
              externalVersion={budgetTreeVersion}
              onAdd={handleAddCategory}
              onEdit={handleEditCategory}
              onDelete={handleDeleteCategory}
            />
          </div>
        </div>
      );
    }

    if (tab.id === 'scheduled') {
      return (
        <ScheduledList
          selectedId={selectedScheduled?.id}
          onSelect={setSelectedScheduled}
          onAdd={handleAddScheduled}
          onEdit={handleEditScheduled}
          onDelete={handleDeleteScheduled}
        />
      );
    }

    // Report tabs
    if (tab.content) {
      const { reportType, options } = tab.content;
      let reportEl = null;
      switch (reportType) {
        case 'pie-income':        reportEl = <PieReport type="I" options={options} />; break;
        case 'pie-expenses':      reportEl = <PieReport type="E" options={options} />; break;
        case 'income-expenses':   reportEl = <IncomeExpensesReport options={options} />; break;
        case 'avg-income-expenses': reportEl = <AverageIncomeExpensesReport options={options} />; break;
        case 'inflow-account':    reportEl = <InflowByAccountReport options={options} />; break;
        case 'inflow-payee':      reportEl = <InflowByPayeeReport options={options} />; break;
        case 'balances-over-time': reportEl = <BalancesOverTimeReport options={options} accountTree={accountTreeNodes} />; break;
        case 'net-worth':         reportEl = <NetWorthOverTimeReport options={options} />; break;
        case 'budget-vs-actual':  reportEl = <BudgetVsActualReport options={options} />; break;
        case 'monthly-cash-flow': reportEl = <MonthlyCashFlowReport options={options} />; break;
        case 'savings-rate':      reportEl = <SavingsRateReport options={options} />; break;
        case 'year-over-year':    reportEl = <YearOverYearReport options={options} />; break;
        case 'top-payees':        reportEl = <TopPayeesBySpendReport options={options} />; break;
        case 'category-drilldown': reportEl = <CategoryDrillDownReport options={options} />; break;
        case 'projected-balance': reportEl = <ProjectedBalanceReport options={options} accountTree={accountTreeNodes} />; break;
        case 'debt-paydown':      reportEl = <DebtPaydownReport options={options} />; break;
        default: return null;
      }
      return (
        <div key={tab.key || 0} className="h-full w-full">
          {reportEl}
        </div>
      );
    }

    return null;
  }

  const activeTabObj = tabs.find(t => t.id === activeTab);

  return (
    <div className="flex flex-col h-full">
      {/* North header */}
      <div className="flex items-center justify-between bg-gradient-to-b from-[#e0e0e0] to-[#c8c8c8] border-b border-gray-400 px-3 py-1 flex-shrink-0 h-10">
        <div className="flex items-center gap-1">
          {userConfig?.encrypted && (
            <span className="text-gray-500" title={t('DATA_ENCRYPTED', 'Data Encrypted')}><Lock size={14} /></span>
          )}
          {userConfig?.premium && (
            <span className="text-yellow-600" title={userConfig?.premiumTooltip || t('PREMIUM_TOOLTIP_HEADER', 'Thanks for donating! Premium features unlocked.')}><Award size={14} /></span>
          )}
        </div>
        <img src="img/logo-title.png" alt={t('BUDDI_LIVE', 'Buddi Live')} className="h-8" />
      </div>

      {/* Tab bar */}
      <TabBar
        tabs={tabs}
        activeTab={activeTab}
        onTabChange={setActiveTab}
        onTabClose={closeTab}
      />

      {/* Main toolbar */}
      <Toolbar>
        {getToolbarItems()}
        <ToolbarSpacer />
        <ToolbarMenu
          icon={<BarChart2 size={13} />}
          label={t('REPORTS', 'Reports')}
          items={reportsMenuItems}
        />
        <ToolbarMenu
          icon={<Settings size={13} />}
          label={t('SYSTEM', 'System')}
          items={systemMenuItems}
        />
        <ToolbarSeparator />
        <ToolbarButton
          icon={<LogOut size={13} />}
          label={t('LOGOUT', 'Logout')}
          onClick={() => { window.location.href = 'authentication/logout'; }}
        />
      </Toolbar>

      {/* Tab content */}
      <div className="flex-1 min-h-0 bg-white overflow-hidden">
        {activeTabObj && renderTabContent(activeTabObj)}
      </div>

      {/* ── Dialogs ── */}
      <AccountEditor
        open={accountEditorOpen}
        selected={accountEditorSelected}
        onClose={() => setAccountEditorOpen(false)}
        onSaved={() => { refreshAccounts(); }}
      />
      <BudgetEditor
        open={budgetEditorOpen}
        selected={budgetEditorSelected}
        onClose={() => setBudgetEditorOpen(false)}
        onSaved={() => setBudgetTreeVersion(v => v + 1)}
      />
      <ScheduledEditor
        open={scheduledEditorOpen}
        selected={scheduledEditorSelected}
        onClose={() => setScheduledEditorOpen(false)}
        onSaved={() => { setSelectedScheduled(null); ScheduledList.reload?.(); }}
      />
      <PreferencesEditor
        open={preferencesOpen}
        onClose={() => setPreferencesOpen(false)}
      />
      <ChangePasswordEditor
        open={changePasswordOpen}
        onClose={() => setChangePasswordOpen(false)}
      />
      <RestoreForm
        open={restoreOpen}
        onClose={() => setRestoreOpen(false)}
      />
      <IntervalPicker
        open={intervalPickerOpen}
        onClose={() => setIntervalPickerOpen(false)}
        onConfirm={options => { intervalPickerCallback && intervalPickerCallback(options); }}
      />
      <CategoryPickerDialog
        open={!!categoryPickerCallback}
        onClose={() => setCategoryPickerCallback(null)}
        onConfirm={cat => { categoryPickerCallback && categoryPickerCallback(cat); }}
      />
      <ProjectedBalancePickerDialog
        open={projectedPickerOpen}
        onClose={() => setProjectedPickerOpen(false)}
        onConfirm={opts => launchReport('projected-balance', opts)}
      />
      {confirmDialog && (
        <ConfirmDialog
          open={true}
          title={confirmDialog.title}
          message={
            confirmDialog.messageType === 'deleteAccountFinalWarning'
              ? renderDeleteAccountWarning({ requireTypedDelete: true })
              : confirmDialog.message
          }
          onConfirm={confirmDialog.onConfirm}
          onCancel={confirmDialog.onCancel || (() => setConfirmDialog(null))}
          confirmLabel={confirmDialog.confirmLabel}
          confirmDisabled={
            confirmDialog.confirmRequiresText != null
              ? deleteAccountConfirmText.trim() !== confirmDialog.confirmRequiresText
              : !!confirmDialog.confirmDisabled
          }
        />
      )}
      {alertDialog && (
        <AlertDialog
          open={true}
          title={alertDialog.title}
          message={alertDialog.message}
          messageHtml={alertDialog.messageHtml}
          onClose={() => setAlertDialog(null)}
        />
      )}
      {error && (
        <AlertDialog
          open={true}
          title={t('ERROR', 'Error')}
          message={error}
          onClose={clearError}
        />
      )}
	      {deletingAccount && (
	        <div className="fixed inset-0 z-[2000] bg-black/40 flex items-center justify-center">
	          <div className="bg-white border border-gray-300 rounded px-6 py-4 shadow-xl text-gray-700 flex flex-col gap-1.5">
	            <div className="text-sm font-semibold">{t('DELETING', 'Deleting...')}</div>
	          </div>
	        </div>
	      )}
    </div>
  );
}

// userConfig is injected by the FreeMarker template via window.BUDDI_CONFIG
function App() {
  const userConfig = window.BUDDI_CONFIG || {};
  return (
    <AppProvider userConfig={userConfig}>
      <BuddiApp userConfig={userConfig} />
    </AppProvider>
  );
}

export default App;
