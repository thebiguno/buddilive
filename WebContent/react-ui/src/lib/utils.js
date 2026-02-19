import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs) {
  return twMerge(clsx(inputs));
}

export async function apiFetch(url, options = {}) {
  const res = await fetch(url, {
    headers: { 'Accept': 'application/json', 'Content-Type': 'application/json', ...options.headers },
    ...options,
  });
  if (!res.ok) {
    let msg = res.statusText;
    try {
      const j = await res.json();
      if (j.msg) msg = j.msg;
    } catch (_) {}
    throw new Error(msg);
  }
  return res.json();
}

export async function apiPost(url, data) {
  return apiFetch(url, { method: 'POST', body: JSON.stringify(data) });
}
