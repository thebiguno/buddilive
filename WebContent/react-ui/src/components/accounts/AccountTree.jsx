import { useState, useEffect, useCallback, useRef } from 'react';
import { ChevronRight, ChevronDown } from 'lucide-react';
import { api } from '../../lib/api';
import { useApp } from '../../context/AppContext';
import { cn } from '../../lib/utils';
import { ContextMenu } from '../ui/ContextMenu';

function flattenVisible(nodes, expandedSet) {
  const result = [];
  function walk(list) {
    for (const n of list) {
      result.push(n);
      if (n.children && n.children.length > 0 && expandedSet.has(n.id)) {
        walk(n.children);
      }
    }
  }
  walk(nodes);
  return result;
}

function parseStyle(styleStr) {
  if (!styleStr) return {};
  const result = {};
  styleStr.split(';').forEach(part => {
    const [key, val] = part.split(':');
    if (key && val) {
      const camel = key.trim().replace(/-([a-z])/g, (_, c) => c.toUpperCase());
      result[camel] = val.trim();
    }
  });
  return result;
}

function TreeNode({ node, depth, selectedId, focusedId, onSelect, expandedSet, onToggle, onContextMenu }) {
  const hasChildren = node.children && node.children.length > 0;
  const isAccount = node.nodeType === 'account';
  const isSelected = selectedId === node.id;
  const isFocused = focusedId === node.id;
  const expanded = expandedSet.has(node.id);

  return (
    <div>
      <div
        className={cn(
          'flex items-center py-0.5 px-1 cursor-pointer select-none text-xs',
          'hover:bg-blue-50',
          depth % 2 === 0 ? 'bg-white' : 'bg-[#f5f5f5]',
          isSelected && '!bg-[#b8d0f0]',
          isFocused && !isSelected && '!bg-blue-100 outline outline-1 outline-blue-400'
        )}
        style={{ paddingLeft: `${depth * 16 + 4}px` }}
        onClick={() => isAccount && onSelect(node)}
        onContextMenu={e => { e.preventDefault(); onContextMenu(e, isAccount ? node : null); }}
      >
        {hasChildren ? (
          <button
            className="mr-1 text-gray-500 hover:text-gray-800 flex-shrink-0"
            onClick={e => { e.stopPropagation(); onToggle(node.id); }}
          >
            {expanded ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
          </button>
        ) : (
          <span className="mr-1 w-3 flex-shrink-0" />
        )}
        <span className="flex-1 truncate" style={parseStyle(node.style)}>{node.name}</span>
        {node.balance != null && (
          <span className="ml-2 text-right flex-shrink-0" style={parseStyle(node.balanceStyle)}>
            {node.balance}
          </span>
        )}
      </div>
      {expanded && hasChildren && node.children.map(child => (
        <TreeNode
          key={child.id}
          node={child}
          depth={depth + 1}
          selectedId={selectedId}
          focusedId={focusedId}
          onSelect={onSelect}
          expandedSet={expandedSet}
          onToggle={onToggle}
          onContextMenu={onContextMenu}
        />
      ))}
    </div>
  );
}

export function AccountTree({ selectedAccount, onAccountSelect, onAdd, onEdit, onDelete }) {
  const { accountTreeVersion, showError, t } = useApp();
  const [nodes, setNodes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [expandedSet, setExpandedSet] = useState(new Set());
  const [focusedId, setFocusedId] = useState(null);
  const containerRef = useRef(null);
  const [ctxMenu, setCtxMenu] = useState(null);

  function handleContextMenu(e, node) {
    if (node) onAccountSelect(node);
    setCtxMenu({ x: e.clientX, y: e.clientY, node });
  }

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await api.accounts.list();
      const children = data.children || [];
      setNodes(children);
      // expand all by default
      const ids = new Set();
      function collectIds(list) { for (const n of list) { ids.add(n.id); if (n.children) collectIds(n.children); } }
      collectIds(children);
      setExpandedSet(ids);
    } catch (e) {
      showError(e);
    } finally {
      setLoading(false);
    }
  }, [showError]);

  useEffect(() => { load(); }, [load, accountTreeVersion]);

  function toggleExpanded(id) {
    setExpandedSet(prev => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id); else next.add(id);
      return next;
    });
  }

  function handleKeyDown(e) {
    const flat = flattenVisible(nodes, expandedSet);
    if (!flat.length) return;
    const currentIdx = focusedId != null ? flat.findIndex(n => n.id === focusedId) : -1;

    if (e.key === 'ArrowDown') {
      e.preventDefault();
      const next = flat[Math.min(currentIdx + 1, flat.length - 1)];
      setFocusedId(next.id);
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      const next = flat[Math.max(currentIdx - 1, 0)];
      setFocusedId(next.id);
    } else if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      const node = flat[currentIdx];
      if (node && node.nodeType === 'account') onAccountSelect(node);
    } else if (e.key === 'ArrowRight') {
      e.preventDefault();
      const node = flat[currentIdx];
      if (node && node.children?.length > 0 && !expandedSet.has(node.id)) toggleExpanded(node.id);
    } else if (e.key === 'ArrowLeft') {
      e.preventDefault();
      const node = flat[currentIdx];
      if (node && expandedSet.has(node.id)) toggleExpanded(node.id);
    }
  }

  return (
    <div
      ref={containerRef}
      className="flex flex-col h-full border-r border-gray-300 outline-none"
      tabIndex={0}
      onKeyDown={handleKeyDown}
    >
      <div className="flex items-center bg-gradient-to-b from-[#d8d8d8] to-[#c8c8c8] border-b border-gray-300 px-2 py-1">
        <span className="text-xs font-semibold text-gray-700 flex-1">{t('NAME', 'Name')}</span>
        <span className="text-xs font-semibold text-gray-700">{t('BALANCE', 'Balance')}</span>
      </div>
      <div
        className="flex-1 overflow-y-auto"
        onContextMenu={e => { if (e.target === e.currentTarget) { e.preventDefault(); setCtxMenu({ x: e.clientX, y: e.clientY, node: null }); } }}
      >
        {loading && <div className="p-2 text-xs text-gray-400">{t('LOADING', 'Loading...')}</div>}
        {nodes.map(node => (
          <TreeNode
            key={node.id}
            node={node}
            depth={0}
            selectedId={selectedAccount?.id}
            focusedId={focusedId}
            onSelect={onAccountSelect}
            expandedSet={expandedSet}
            onToggle={toggleExpanded}
            onContextMenu={handleContextMenu}
          />
        ))}
      </div>
      {ctxMenu && (
        <ContextMenu
          x={ctxMenu.x}
          y={ctxMenu.y}
          onClose={() => setCtxMenu(null)}
          items={[
            { label: t('NEW_ACCOUNT', 'New Account'), onClick: () => onAdd?.() },
            '-',
            { label: t('MODIFY_ACCOUNT', 'Edit Account'), disabled: !ctxMenu.node, onClick: () => onEdit?.() },
            { label: t('DELETE_ACCOUNT', 'Delete Account'), disabled: !ctxMenu.node, onClick: () => onDelete?.() },
          ]}
        />
      )}
    </div>
  );
}
