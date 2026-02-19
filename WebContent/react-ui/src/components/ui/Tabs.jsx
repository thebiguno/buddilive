import { cn } from '../../lib/utils';

export function TabBar({ tabs, activeTab, onTabChange, onTabClose, className }) {
  return (
    <div className={cn('flex items-end gap-0 bg-[#c8c8c8] border-b border-gray-400 flex-shrink-0 overflow-x-auto', className)}>
      {tabs.map(tab => (
        <div
          key={tab.id}
          className={cn(
            'flex items-center gap-1 px-3 py-1.5 text-xs border-r border-gray-400 cursor-pointer select-none flex-shrink-0',
            'hover:bg-[#e0e0e0] transition-colors',
            activeTab === tab.id
              ? 'bg-white border-t-2 border-t-blue-500 -mb-px pb-2 font-medium'
              : 'bg-[#d8d8d8] mt-0.5'
          )}
          onClick={() => onTabChange(tab.id)}
        >
          <span>{tab.label}</span>
          {tab.closable && (
            <button
              className="ml-1 text-gray-400 hover:text-gray-700 rounded-full w-3.5 h-3.5 flex items-center justify-center text-[10px] leading-none"
              onClick={e => { e.stopPropagation(); onTabClose && onTabClose(tab.id); }}
            >
              ×
            </button>
          )}
        </div>
      ))}
    </div>
  );
}
