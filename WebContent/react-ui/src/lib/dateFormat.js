export function todayIso() {
  return new Date().toISOString().split('T')[0];
}

export function normalizeDateFormat(rawFormat) {
  const normalized = String(rawFormat || '')
    .trim()
    .replace(/\s+/g, '')
    .replace(/Y/g, 'y')
    .replace(/D/g, 'd')
    .replace(/M/g, 'm')
    .toLowerCase();

  if (/^y{1,4}[-./]m{1,4}[-./]d{1,4}$/.test(normalized)) return 'yyyy-MM-dd';
  if (/^m{1,4}[-./]d{1,4}[-./]y{1,4}$/.test(normalized)) return 'MM/dd/yyyy';
  if (/^d{1,4}[./-]m{1,4}[./-]y{1,4}$/.test(normalized)) {
    return normalized.includes('.') ? 'dd.MM.yyyy' : 'dd/MM/yyyy';
  }

  return 'yyyy-MM-dd';
}

export function getDatePlaceholder(dateFormat) {
  if (dateFormat === 'dd.MM.yyyy') return 'dd.mm.yyyy';
  if (dateFormat === 'dd/MM/yyyy') return 'dd/mm/yyyy';
  if (dateFormat === 'MM/dd/yyyy') return 'mm/dd/yyyy';
  return 'yyyy-mm-dd';
}

export function parseIsoDate(value) {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(String(value || '').trim());
  if (!match) return new Date(Number.NaN);
  const y = Number.parseInt(match[1], 10);
  const m = Number.parseInt(match[2], 10);
  const d = Number.parseInt(match[3], 10);
  return new Date(y, m - 1, d);
}

export function isValidDateObject(value) {
  return value instanceof Date && !Number.isNaN(value.getTime());
}

export function toIsoDate(date) {
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

export function formatDateForDisplay(date, dateFormat) {
  if (!isValidDateObject(date)) return '';
  const yyyy = String(date.getFullYear());
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  if (dateFormat === 'dd.MM.yyyy') return `${dd}.${mm}.${yyyy}`;
  if (dateFormat === 'dd/MM/yyyy') return `${dd}/${mm}/${yyyy}`;
  if (dateFormat === 'MM/dd/yyyy') return `${mm}/${dd}/${yyyy}`;
  return `${yyyy}-${mm}-${dd}`;
}

export function formatIsoDateForDisplay(isoDate, dateFormat) {
  const parsed = parseIsoDate(isoDate);
  if (!isValidDateObject(parsed) || toIsoDate(parsed) !== isoDate) {
    return formatDateForDisplay(parseIsoDate(todayIso()), dateFormat);
  }
  return formatDateForDisplay(parsed, dateFormat);
}

export function parseDisplayDate(raw, dateFormat, fallbackDate = parseIsoDate(todayIso())) {
  const value = String(raw || '').trim();
  if (!value) return null;
  return parseFlexibleDateInput(value, fallbackDate, dateFormat);
}

export function normalizeDateInput(raw, fallbackDisplayValue, dateFormat) {
  const value = String(raw || '').trim();
  if (!value) return '';

  const base = parseDisplayDate(fallbackDisplayValue, dateFormat) || parseIsoDate(todayIso());
  const parsed = parseFlexibleDateInput(value, base, dateFormat);
  return parsed ? formatDateForDisplay(parsed, dateFormat) : value;
}

function parseFlexibleDateInput(raw, baseDate, dateFormat) {
  const value = String(raw || '').trim();
  if (!value) return null;
  if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    const iso = parseIsoDate(value);
    if (isValidDateObject(iso) && toIsoDate(iso) === value) return iso;
  }

  const numericTokens = value
    .split(/[^0-9]+/)
    .map(v => v.trim())
    .filter(Boolean);
  const digits = value.replace(/\D/g, '');

  if (numericTokens.length === 3) {
    const [a, b, c] = numericTokens.map(v => Number.parseInt(v, 10));
    if (numericTokens[0].length === 4) return buildValidDate(a, b, c); // YYYY-M-D
    if (numericTokens[2].length === 4) return firstValidDate(buildDateCandidatesForYear(c, a, b, dateFormat));
    return firstValidDate(buildDateCandidatesWithTwoDigitYear(a, b, c, dateFormat));
  }

  if (numericTokens.length === 2) {
    const [a, b] = numericTokens.map(v => Number.parseInt(v, 10));
    if (numericTokens[0].length === 4) return buildValidDate(a, b, 1); // YYYY-M -> day 1
    return firstValidDate(buildDateCandidatesForYear(baseDate.getFullYear(), a, b, dateFormat));
  }

  if (numericTokens.length === 1 && digits.length === 8) {
    return firstValidDate(buildDateCandidatesFromEightDigits(digits, dateFormat));
  }

  if (numericTokens.length === 1 && digits.length === 6) {
    return firstValidDate(buildDateCandidatesFromSixDigits(digits, dateFormat));
  }

  if (/[a-z]/i.test(value)) {
    const native = new Date(value);
    if (!Number.isNaN(native.getTime())) {
      return buildValidDate(native.getFullYear(), native.getMonth() + 1, native.getDate());
    }
  }
  return null;
}

function buildDateCandidatesForYear(year, first, second, dateFormat) {
  if (dateFormat === 'dd/MM/yyyy' || dateFormat === 'dd.MM.yyyy') {
    return [
      buildValidDate(year, second, first),
      buildValidDate(year, first, second),
    ];
  }
  if (dateFormat === 'MM/dd/yyyy') {
    return [
      buildValidDate(year, first, second),
      buildValidDate(year, second, first),
    ];
  }
  return [
    buildValidDate(year, first, second),
    buildValidDate(year, second, first),
  ];
}

function buildDateCandidatesWithTwoDigitYear(first, second, yearToken, dateFormat) {
  const yearFromEnd = expandTwoDigitYear(yearToken);
  const candidates = [...buildDateCandidatesForYear(yearFromEnd, first, second, dateFormat)];
  if (dateFormat === 'yyyy-MM-dd') {
    candidates.unshift(buildValidDate(expandTwoDigitYear(first), second, yearToken));
  }
  return candidates;
}

function buildDateCandidatesFromEightDigits(digits, dateFormat) {
  const yFirst = Number.parseInt(digits.slice(0, 4), 10);
  const mFirst = Number.parseInt(digits.slice(4, 6), 10);
  const dFirst = Number.parseInt(digits.slice(6, 8), 10);
  const firstAsMonth = Number.parseInt(digits.slice(0, 2), 10);
  const secondAsDay = Number.parseInt(digits.slice(2, 4), 10);
  const trailingYear = Number.parseInt(digits.slice(4, 8), 10);

  if (dateFormat === 'dd/MM/yyyy' || dateFormat === 'dd.MM.yyyy') {
    return [
      buildValidDate(trailingYear, secondAsDay, firstAsMonth),
      buildValidDate(yFirst, mFirst, dFirst),
      buildValidDate(trailingYear, firstAsMonth, secondAsDay),
    ];
  }
  if (dateFormat === 'MM/dd/yyyy') {
    return [
      buildValidDate(trailingYear, firstAsMonth, secondAsDay),
      buildValidDate(yFirst, mFirst, dFirst),
      buildValidDate(trailingYear, secondAsDay, firstAsMonth),
    ];
  }
  return [
    buildValidDate(yFirst, mFirst, dFirst),
    buildValidDate(trailingYear, firstAsMonth, secondAsDay),
    buildValidDate(trailingYear, secondAsDay, firstAsMonth),
  ];
}

function buildDateCandidatesFromSixDigits(digits, dateFormat) {
  const firstTwo = Number.parseInt(digits.slice(0, 2), 10);
  const middleTwo = Number.parseInt(digits.slice(2, 4), 10);
  const lastTwoYear = expandTwoDigitYear(Number.parseInt(digits.slice(4, 6), 10));

  const firstYear = expandTwoDigitYear(firstTwo);
  const middleMonth = middleTwo;
  const lastDay = Number.parseInt(digits.slice(4, 6), 10);

  if (dateFormat === 'dd/MM/yyyy' || dateFormat === 'dd.MM.yyyy') {
    return [
      buildValidDate(lastTwoYear, middleTwo, firstTwo),
      buildValidDate(firstYear, middleMonth, lastDay),
      buildValidDate(lastTwoYear, firstTwo, middleTwo),
    ];
  }
  if (dateFormat === 'MM/dd/yyyy') {
    return [
      buildValidDate(lastTwoYear, firstTwo, middleTwo),
      buildValidDate(firstYear, middleMonth, lastDay),
      buildValidDate(lastTwoYear, middleTwo, firstTwo),
    ];
  }
  return [
    buildValidDate(firstYear, middleMonth, lastDay),
    buildValidDate(lastTwoYear, firstTwo, middleTwo),
    buildValidDate(lastTwoYear, middleTwo, firstTwo),
  ];
}

function firstValidDate(candidates) {
  for (const c of candidates) {
    if (c) return c;
  }
  return null;
}

function buildValidDate(year, month, day) {
  if (![year, month, day].every(Number.isFinite)) return null;
  if (year < 100 || month < 1 || month > 12 || day < 1 || day > 31) return null;
  const dt = new Date(year, month - 1, day);
  if (dt.getFullYear() !== year || dt.getMonth() !== month - 1 || dt.getDate() !== day) return null;
  return dt;
}

function expandTwoDigitYear(y) {
  if (!Number.isFinite(y)) return y;
  if (y < 100) return y >= 70 ? 1900 + y : 2000 + y;
  return y;
}
