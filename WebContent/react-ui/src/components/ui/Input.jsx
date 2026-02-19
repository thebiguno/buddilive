import { cn } from '../../lib/utils';

export function Input({ className, ...props }) {
  return (
    <input
      className={cn(
        'border border-gray-400 rounded px-1.5 py-0.5 text-xs bg-white',
        'focus:outline-none focus:ring-1 focus:ring-blue-400 focus:border-blue-400',
        'disabled:bg-gray-100 disabled:text-gray-500',
        className
      )}
      {...props}
    />
  );
}

export function Textarea({ className, ...props }) {
  return (
    <textarea
      className={cn(
        'border border-gray-400 rounded px-1.5 py-0.5 text-xs bg-white',
        'focus:outline-none focus:ring-1 focus:ring-blue-400 focus:border-blue-400',
        'disabled:bg-gray-100 disabled:text-gray-500 resize-y',
        className
      )}
      {...props}
    />
  );
}
