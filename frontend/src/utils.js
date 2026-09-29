export function money(value = 0, currency = 'INR') {
  try {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency', currency: currency || 'INR', maximumFractionDigits: 2,
    }).format(Number(value || 0));
  } catch {
    return `${currency || ''} ${Number(value || 0).toFixed(2)}`.trim();
  }
}

export function shortMoney(value = 0, currency = 'INR') {
  try {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency', currency: currency || 'INR', notation: 'compact', maximumFractionDigits: 1,
    }).format(Number(value || 0));
  } catch { return money(value, currency); }
}

export function dateTime(value) {
  if (!value) return '—';
  return new Intl.DateTimeFormat('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit',
  }).format(new Date(value));
}

export function dateOnly(value) {
  if (!value) return '—';
  return new Intl.DateTimeFormat('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric',
  }).format(new Date(value));
}

export function initials(name = 'Secure Bank') {
  return name.trim().split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase() || 'SB';
}

export function firstName(name = 'there') {
  return name.trim().split(/\s+/)[0] || 'there';
}

export function maskAccount(number = '') {
  if (!number) return '•••• •••• ••••';
  return `•••• •••• ${number.slice(-4)}`;
}

export function titleCase(value = '') {
  return value.toLowerCase().replaceAll('_', ' ').replace(/\b\w/g, (letter) => letter.toUpperCase());
}

export function localDateTimeValue(date) {
  const d = new Date(date);
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 16);
}

export function greeting() {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 17) return 'Good afternoon';
  return 'Good evening';
}
