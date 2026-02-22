import { useState, useEffect, useCallback, useRef } from 'react';
import { ChevronRight, ChevronDown } from 'lucide-react';
import { api } from '../../lib/api';
import { useApp } from '../../context/AppContext';
import { Input } from '../ui/Input';
import { cn } from '../../lib/utils';
import { ContextMenu } from '../ui/ContextMenu';
import { formatLocaleNumber, parseLocaleNumber } from '../../lib/numberFormat';

function parseStyle(styleStr) {
  if (!styleStr) return {};
  const result = {};
  (styleStr || '').split(';').forEach(part => {
    const [key, val] = part.split(':');
    if (key && val) {
      const camel = key.trim().replace(/-([a-z])/g, (_, c) => c.toUpperCase());
      result[camel] = val.trim();
    }
  });
  return result;
}

function BudgetRow({ node, depth, selectedId, onSelect, onEditAmount, stripe, onContextMenu, t, locale }) {
  const [expanded, setExpanded] = useState(true);
  const [editing, setEditing] = useState(false);
  const [editValue, setEditValue] = useState('');
  const editInputRef = useCallback(el => {
    if (el) { el.focus(); el.select(); }
  }, []);
  const hasChildren = node.children && node.children.length > 0;
  const isSelected = selectedId === node.id;

  function startEdit() {
    const n = parseLocaleNumber(node.currentAmount, locale);
    setEditValue(Number.isFinite(n) ? formatLocaleNumber(n, locale, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '');
    setEditing(true);
  }

  function commitEdit() {
    setEditing(false);
    const n = parseLocaleNumber(editValue, locale);
    if (onEditAmount) onEditAmount(node, Number.isFinite(n) ? Number(n.toFixed(2)) : 0);
  }

  return (
    <>
      <div
        className={cn(
          'flex items-center text-xs border-b border-gray-200 cursor-pointer select-none',
          'hover:bg-blue-50',
          isSelected ? '!bg-[#b8d0f0]' : (stripe ? 'bg-[#f5f5f5]' : 'bg-white')
        )}
        onClick={() => onSelect && onSelect(node)}
        onContextMenu={e => { e.preventDefault(); onContextMenu(e, node); }}
      >
        {/* Name cell */}
        <div
          className="flex items-center w-[28%] py-0.5 border-r border-gray-200 truncate"
          style={{ paddingLeft: `${depth * 16 + 4}px` }}
        >
          {hasChildren ? (
            <button
              className="mr-1 text-gray-500 hover:text-gray-800 flex-shrink-0"
              onClick={e => { e.stopPropagation(); setExpanded(v => !v); }}
            >
              {expanded ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
            </button>
          ) : (
            <span className="mr-1 w-3 flex-shrink-0" />
          )}
          <span style={parseStyle(node.nameStyle)} className="truncate">{node.name}</span>
        </div>
        {/* Previous */}
        <div className="w-[18%] text-right px-2 border-r border-gray-200 py-0.5 flex-shrink-0">
          <span style={parseStyle(node.previousStyle)}>{node.previous}</span>
        </div>
        {/* Current (editable) */}
        <div
          className="w-[18%] text-right px-2 border-r border-gray-200 py-0.5 flex-shrink-0"
          onClick={e => { e.stopPropagation(); startEdit(); }}
        >
          {editing ? (
            <input
              ref={editInputRef}
              type="text"
              inputMode="decimal"
              className="w-full text-right border border-blue-400 rounded px-1 text-xs [appearance:textfield]"
              value={editValue}
              onChange={e => setEditValue(e.target.value)}
              onBlur={commitEdit}
              onKeyDown={e => { if (e.key === 'Enter') commitEdit(); if (e.key === 'Escape') { setEditing(false); } }}
              onClick={e => e.stopPropagation()}
            />
          ) : (
            <span style={parseStyle(node.currentStyle)} className={cn(!node.current && 'text-gray-400 italic')}>
              {node.current || t('CLICK_TO_ENTER', 'Click to enter')}
            </span>
          )}
        </div>
        {/* Actual */}
        <div className="w-[18%] text-right px-2 border-r border-gray-200 py-0.5 flex-shrink-0">
          <span style={parseStyle(node.actualStyle)}>{node.actual}</span>
        </div>
        {/* Difference */}
        <div className="w-[18%] text-right px-2 py-0.5 flex-shrink-0">
          <span style={parseStyle(node.differenceStyle)}>{node.difference}</span>
        </div>
      </div>
      {expanded && hasChildren && node.children.map((child, i) => (
        <BudgetRow
          key={child.id}
          node={child}
          depth={depth + 1}
          selectedId={selectedId}
          onSelect={onSelect}
          onEditAmount={onEditAmount}
          stripe={i % 2 !== 0}
          onContextMenu={onContextMenu}
          t={t}
          locale={locale}
        />
      ))}
    </>
  );
}

export function BudgetTree({ periodType, onSelectionChange, externalVersion = 0, onAdd, onEdit, onDelete }) {
  const { showError, t, userConfig } = useApp();
  const locale = userConfig?.locale;
  const [nodes, setNodes] = useState([]);
  const [period, setPeriod] = useState('');
  const [previousPeriod, setPreviousPeriod] = useState('');
  const [currentDate, setCurrentDate] = useState(null);
  const [loading, setLoading] = useState(false);
  const [selectedNode, setSelectedNode] = useState(null);
  const [version, setVersion] = useState(0);
  // navDateRef holds the clean start-of-period date returned by the server.
  // Using a ref avoids triggering useEffect when load() updates it.
  const navDateRef = useRef(null);
  const [ctxMenu, setCtxMenu] = useState(null);

  function handleContextMenu(e, node) {
    if (node) { setSelectedNode(node); onSelectionChange && onSelectionChange(node); }
    setCtxMenu({ x: e.clientX, y: e.clientY, node });
  }

  const load = useCallback(async (offset = 0) => {
    setLoading(true);
    try {
      const dateParam = navDateRef.current;
      const url = `data/categories.json?periodType=${periodType}${dateParam ? `&date=${dateParam}` : ''}${offset !== 0 ? `&offset=${offset}` : ''}`;
      const data = await api.get(url);
      setNodes(data.children || []);
      setPeriod(data.period || '');
      setPreviousPeriod(data.previousPeriod || '');
      const returnedDate = data.date || null;
      setCurrentDate(returnedDate);
      // Normalise to the clean server date so next version-bump stays on this period
      navDateRef.current = returnedDate;
    } catch (e) {
      showError(e);
    } finally {
      setLoading(false);
    }
  }, [periodType, showError]);

  useEffect(() => { load(0); }, [load, version, externalVersion]);

  function handleSelect(node) {
    setSelectedNode(node);
    onSelectionChange && onSelectionChange(node);
  }

  async function handleEditAmount(node, amount) {
    try {
      await api.categories.save({
        action: 'set',
        categoryId: node.id,
        amount,
        date: node.date || currentDate,
        periodType: node.type || periodType,
      });
      setVersion(v => v + 1);
    } catch (e) {
      showError(e);
    }
  }

  function goToPrevious() {
    load(-1);
  }
  function goToNext() {
    load(1);
  }

  async function copyFromPrevious() {
    try {
      await api.categories.save({ action: 'copyFromPrevious', type: periodType, date: currentDate });
      setVersion(v => v + 1);
    } catch (e) {
      showError(e);
    }
  }

  return (
    <div className="flex flex-col h-full">
      {/* Toolbar */}
      <div className="flex items-center gap-2 px-2 py-1 bg-gradient-to-b from-[#e8e8e8] to-[#d0d0d0] border-b border-gray-300 flex-shrink-0">
        <button
          className="text-xs px-2 py-0.5 rounded border border-gray-400 bg-white hover:bg-gray-100 cursor-pointer"
          onClick={copyFromPrevious}
        >
          {t('COPY_FROM_PREVIOUS_PERIOD', 'Copy from Previous Period')}
        </button>
        <div className="flex-1" />
        <span className="text-xs text-gray-600">{t('CURRENT_PERIOD', 'Current Period')}</span>
        <button
          className="w-5 h-5 flex items-center justify-center rounded border border-gray-400 bg-white hover:bg-gray-100 cursor-pointer"
          onClick={goToPrevious}
          title={t('PREVIOUS_PERIOD', 'Previous period')}
        >
          ‹
        </button>
        <span className="text-xs font-medium w-52 text-center whitespace-nowrap">{period}</span>
        <button
          className="w-5 h-5 flex items-center justify-center rounded border border-gray-400 bg-white hover:bg-gray-100 cursor-pointer"
          onClick={goToNext}
          title={t('NEXT_PERIOD', 'Next period')}
        >
          ›
        </button>
      </div>
      {/* Header row */}
      <div className="flex items-center text-xs font-semibold bg-gradient-to-b from-[#d8d8d8] to-[#c8c8c8] border-b border-gray-300 flex-shrink-0">
        <div className="w-[28%] px-2 py-1 border-r border-gray-300">{t('NAME', 'Name')}</div>
        <div className="w-[18%] text-right px-2 py-1 border-r border-gray-300">
          {t('PREVIOUS', 'Previous')}{previousPeriod ? ` (${previousPeriod})` : ''}
        </div>
        <div className="w-[18%] text-right px-2 py-1 border-r border-gray-300">
          {t('CURRENT', 'Current')}{period ? ` (${period})` : ''}
        </div>
        <div className="w-[18%] text-right px-2 py-1 border-r border-gray-300">{t('ACTUAL', 'Actual')}</div>
        <div className="w-[18%] text-right px-2 py-1">{t('REMAINING', 'Remaining')}</div>
      </div>
      {/* Rows */}
      <div
        className="flex-1 overflow-y-auto"
        onContextMenu={e => { if (e.target === e.currentTarget) { e.preventDefault(); setCtxMenu({ x: e.clientX, y: e.clientY, node: null }); } }}
      >
        {loading && <div className="p-2 text-xs text-gray-400">{t('LOADING', 'Loading...')}</div>}
        {nodes.map((node, i) => (
          <BudgetRow
            key={node.id}
            node={node}
            depth={0}
            selectedId={selectedNode?.id}
            onSelect={handleSelect}
            onEditAmount={handleEditAmount}
            stripe={i % 2 !== 0}
            onContextMenu={handleContextMenu}
            t={t}
            locale={locale}
          />
        ))}
      </div>
      {ctxMenu && (
        <ContextMenu
          x={ctxMenu.x}
          y={ctxMenu.y}
          onClose={() => setCtxMenu(null)}
          items={[
            { label: t('NEW_BUDGET_CATEGORY', 'New Category'), onClick: () => onAdd?.() },
            '-',
            { label: t('MODIFY_BUDGET_CATEGORY', 'Edit Category'), disabled: !ctxMenu.node, onClick: () => onEdit?.() },
            { label: t('DELETE_BUDGET_CATEGORY', 'Delete Category'), disabled: !ctxMenu.node, onClick: () => onDelete?.() },
          ]}
        />
      )}
    </div>
  );
}
