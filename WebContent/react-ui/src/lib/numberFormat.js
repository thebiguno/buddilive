export function resolveIntlLocale(localeValue) {
  const raw = String(localeValue || '').trim();
  if (!raw) return undefined;

  const normalized = raw.replace(/_/g, '-');
  try {
    const [supported] = Intl.NumberFormat.supportedLocalesOf([normalized]);
    return supported || undefined;
  } catch (_) {
    return undefined;
  }
}

export function resolveCurrencyCode(currencyValue) {
  const raw = String(currencyValue || '').trim().toUpperCase();
  return /^[A-Z]{3}$/.test(raw) ? raw : 'USD';
}

export function formatLocaleNumber(value, localeValue, options = {}) {
  const n = typeof value === 'number' ? value : Number(value);
  if (!Number.isFinite(n)) return '';

  const locale = resolveIntlLocale(localeValue);
  const {
    minimumFractionDigits = 2,
    maximumFractionDigits = 2,
    useGrouping = true,
  } = options;

  return new Intl.NumberFormat(locale || undefined, {
    useGrouping,
    minimumFractionDigits,
    maximumFractionDigits,
  }).format(n);
}

export function formatLocaleCurrency(value, localeValue, currencyValue, options = {}) {
  const n = typeof value === 'number' ? value : Number(value);
  if (!Number.isFinite(n)) return '';

  const locale = resolveIntlLocale(localeValue);
  const currency = resolveCurrencyCode(currencyValue);
  const {
    minimumFractionDigits = 2,
    maximumFractionDigits = 2,
    notation,
  } = options;

  return new Intl.NumberFormat(locale || undefined, {
    style: 'currency',
    currency,
    useGrouping: true,
    minimumFractionDigits,
    maximumFractionDigits,
    ...(notation ? { notation } : {}),
  }).format(n);
}

export function parseLocaleNumber(rawValue, localeValue) {
  if (typeof rawValue === 'number') return rawValue;

  const raw = String(rawValue || '').trim();
  if (!raw) return Number.NaN;

  const locale = resolveIntlLocale(localeValue);
  const { decimalSymbol, groupSymbol } = getNumberSymbols(locale);

  let value = raw
    .replace(/[\u00A0\u202F\s]/g, '')
    .replace(/[−–—]/g, '-')
    .replace(/[^\d.,+\-']/g, '');

  if (!value) return Number.NaN;

  const dotCount = (value.match(/\./g) || []).length;
  const commaCount = (value.match(/,/g) || []).length;
  const decimalChar = detectDecimalCharacter(value, decimalSymbol, dotCount, commaCount);

  value = value.replace(/'/g, '');
  if (groupSymbol && groupSymbol !== decimalChar && groupSymbol !== '.' && groupSymbol !== ',') {
    value = value.split(groupSymbol).join('');
  }

  if (decimalChar === '.') {
    value = value.replace(/,/g, '');
  } else if (decimalChar === ',') {
    value = value.replace(/\./g, '').replace(',', '.');
  } else {
    value = value.replace(/[.,]/g, '');
  }

  value = value.replace(/(?!^)[+\-]/g, '');
  if (value === '' || value === '+' || value === '-') return Number.NaN;

  const parsed = Number.parseFloat(value);
  return Number.isFinite(parsed) ? parsed : Number.NaN;
}

export function getAmountPlaceholder(localeValue) {
  return formatLocaleNumber(0, localeValue, {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
    useGrouping: false,
  }) || '0.00';
}

function getNumberSymbols(locale) {
  const parts = new Intl.NumberFormat(locale || undefined).formatToParts(12345.6);
  const decimalSymbol = parts.find(p => p.type === 'decimal')?.value || '.';
  const groupSymbol = parts.find(p => p.type === 'group')?.value || ',';
  return { decimalSymbol, groupSymbol };
}

function detectDecimalCharacter(value, localeDecimal, dotCount, commaCount) {
  if (dotCount > 0 && commaCount > 0) {
    return value.lastIndexOf('.') > value.lastIndexOf(',') ? '.' : ',';
  }

  if (dotCount > 0) {
    if (localeDecimal === '.') return '.';
    if (dotCount > 1) return null;
    return digitsAfter(value, '.') <= 2 ? '.' : null;
  }

  if (commaCount > 0) {
    if (localeDecimal === ',') return ',';
    if (commaCount > 1) return null;
    return digitsAfter(value, ',') <= 2 ? ',' : null;
  }

  return null;
}

function digitsAfter(value, symbol) {
  const idx = value.lastIndexOf(symbol);
  if (idx < 0) return 0;
  let count = 0;
  for (let i = idx + 1; i < value.length; i++) {
    if (/\d/.test(value[i])) count++;
  }
  return count;
}
