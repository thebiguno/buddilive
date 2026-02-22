import { Plus, Minus, ArrowRight } from 'lucide-react';
import { Input } from '../ui/Input';
import { Combobox } from '../ui/Combobox';
import { cn } from '../../lib/utils';
import { useApp } from '../../context/AppContext';
import { formatLocaleNumber, getAmountPlaceholder, parseLocaleNumber } from '../../lib/numberFormat';

export function SplitEditor({ split, index, isOnly, splitSources, onUpdate, onAdd, onRemove }) {
  const { t, userConfig } = useApp();
  const { from: fromOptions, to: toOptions } = splitSources;
  const locale = userConfig?.locale;
  const amountPlaceholder = getAmountPlaceholder(locale);

  function update(field, value) {
    onUpdate(index, { ...split, [field]: value });
  }

  function handleFromSelect(opt) {
    const updated = { ...split, fromId: opt.value, fromType: opt.type };
    // If source is set and the selected is not the source, auto-set other side to source
    if (split.source && opt.value !== split.source) {
      updated.toId = split.source;
    }
    if (opt.value === updated.toId) updated.toId = null;
    onUpdate(index, updated);
  }

  function handleToSelect(opt) {
    const updated = { ...split, toId: opt.value, toType: opt.type };
    if (split.source && opt.value !== split.source) {
      updated.fromId = split.source;
    }
    if (opt.value === updated.fromId) updated.fromId = null;
    onUpdate(index, updated);
  }

  return (
    <div className="flex items-center gap-1 py-0.5">
      <Input
        type="text"
        inputMode="decimal"
        className="w-24 text-right"
        value={split.amount || ''}
        onChange={e => update('amount', e.target.value)}
        onBlur={e => {
          const n = parseLocaleNumber(e.target.value, locale);
          if (Number.isFinite(n)) {
            update('amount', formatLocaleNumber(n, locale, { minimumFractionDigits: 2, maximumFractionDigits: 2 }));
          }
        }}
        placeholder={amountPlaceholder || t('AMOUNT_PLACEHOLDER', '0.00')}
      />
      <Combobox
        className="flex-1 min-w-0"
        options={fromOptions}
        value={split.fromId}
        onSelect={handleFromSelect}
        placeholder={t('FROM', 'From')}
      />
      <ArrowRight size={12} className="text-gray-400 flex-shrink-0" />
      <Combobox
        className="flex-1 min-w-0"
        options={toOptions}
        value={split.toId}
        onSelect={handleToSelect}
        placeholder={t('TO', 'To')}
      />
      <Input
        className="flex-1 min-w-0"
        value={split.memo || ''}
        onChange={e => update('memo', e.target.value)}
        placeholder={t('MEMO', 'Memo')}
      />
      <button
        className={cn(
          'flex-shrink-0 w-5 h-5 rounded-full flex items-center justify-center text-white text-xs',
          isOnly ? 'bg-gray-300 cursor-not-allowed' : 'bg-red-400 hover:bg-red-500 cursor-pointer'
        )}
        onClick={() => !isOnly && onRemove(index)}
        disabled={isOnly}
        title={t('REMOVE_SPLIT', 'Remove split')}
      >
        <Minus size={10} />
      </button>
      <button
        className="flex-shrink-0 w-5 h-5 rounded-full bg-green-500 hover:bg-green-600 flex items-center justify-center text-white cursor-pointer"
        onClick={() => onAdd(index)}
        title={t('ADD_SPLIT', 'Add split')}
      >
        <Plus size={10} />
      </button>
    </div>
  );
}
