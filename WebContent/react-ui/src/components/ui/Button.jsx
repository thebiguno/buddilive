import { cn } from '../../lib/utils';

export function Button({ className, variant = 'default', size = 'sm', disabled, children, ...props }) {
  const base = 'inline-flex items-center gap-1 rounded border cursor-pointer font-normal transition-colors focus:outline-none focus:ring-1 focus:ring-blue-400 disabled:opacity-50 disabled:cursor-not-allowed';
  const variants = {
    default: 'bg-gradient-to-b from-[#f5f5f5] to-[#e0e0e0] border-gray-400 text-gray-800 hover:from-[#eaeaea] hover:to-[#d0d0d0] active:from-[#d8d8d8]',
    primary: 'bg-gradient-to-b from-[#6090d0] to-[#4070b0] border-[#3060a0] text-white hover:from-[#5080c0] hover:to-[#3060a0]',
    danger: 'bg-gradient-to-b from-[#f08080] to-[#d06060] border-[#b04040] text-white hover:from-[#e07070] hover:to-[#c05050]',
    ghost: 'bg-transparent border-transparent text-gray-700 hover:bg-gray-200',
    toolbar: 'bg-gradient-to-b from-[#f5f5f5] to-[#e0e0e0] border-gray-400 text-gray-800 hover:from-[#eaeaea] hover:to-[#d0d0d0] px-2 py-1 text-xs',
  };
  const sizes = {
    xs: 'px-1.5 py-0.5 text-xs',
    sm: 'px-2 py-1 text-xs',
    md: 'px-3 py-1.5 text-sm',
  };
  return (
    <button
      className={cn(base, variants[variant], sizes[size], className)}
      disabled={disabled}
      {...props}
    >
      {children}
    </button>
  );
}
