import React from 'react';

const paths = {
  grid: <><rect x="3" y="3" width="7" height="7" rx="2"/><rect x="14" y="3" width="7" height="7" rx="2"/><rect x="3" y="14" width="7" height="7" rx="2"/><rect x="14" y="14" width="7" height="7" rx="2"/></>,
  transfer: <><path d="M7 7h11l-3-3"/><path d="m18 7-3 3"/><path d="M17 17H6l3 3"/><path d="m6 17 3-3"/></>,
  card: <><rect x="2.5" y="5" width="19" height="14" rx="3"/><path d="M2.5 10h19"/><path d="M6 15h3"/></>,
  document: <><path d="M6 2.5h8l4 4V21H6z"/><path d="M14 2.5v5h5"/><path d="M9 12h6M9 16h6"/></>,
  bell: <><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"/><path d="M10 21h4"/></>,
  help: <><circle cx="12" cy="12" r="9"/><path d="M9.8 9a2.4 2.4 0 1 1 3.4 2.2c-.8.4-1.2.9-1.2 1.8"/><path d="M12 17h.01"/></>,
  user: <><circle cx="12" cy="8" r="4"/><path d="M4.5 21a7.5 7.5 0 0 1 15 0"/></>,
  logout: <><path d="M10 5H5v14h5"/><path d="M14 8l4 4-4 4M18 12H9"/></>,
  menu: <path d="M4 7h16M4 12h16M4 17h16"/>,
  close: <path d="m6 6 12 12M18 6 6 18"/>,
  shield: <><path d="M12 2.5 20 6v6c0 5-3.4 8.5-8 10-4.6-1.5-8-5-8-10V6z"/><path d="m8.5 12 2.2 2.2 4.8-5"/></>,
  chevron: <path d="m8 10 4 4 4-4"/>,
  chevronRight: <path d="m9 18 6-6-6-6"/>,
  arrowRight: <path d="M5 12h14m-5-5 5 5-5 5"/>,
  arrowLeft: <path d="M19 12H5m5-5-5 5 5 5"/>,
  mail: <><rect x="3" y="5" width="18" height="14" rx="2"/><path d="m4 7 8 6 8-6"/></>,
  lock: <><rect x="4" y="10" width="16" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></>,
  eye: <><path d="M2.5 12s3.5-6 9.5-6 9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6Z"/><circle cx="12" cy="12" r="2.5"/></>,
  eyeOff: <><path d="m3 3 18 18"/><path d="M10.6 6.2A10.7 10.7 0 0 1 12 6c6 0 9.5 6 9.5 6a17 17 0 0 1-2.2 3"/><path d="M6.4 6.5A16.7 16.7 0 0 0 2.5 12s3.5 6 9.5 6c1 0 2-.2 2.8-.5"/></>,
  phone: <path d="M7 3H4.5A1.5 1.5 0 0 0 3 4.5 16.5 16.5 0 0 0 19.5 21a1.5 1.5 0 0 0 1.5-1.5V17l-4-1-1.2 3a14.4 14.4 0 0 1-10.8-10L8 7z"/>,
  wallet: <><path d="M4 6h14a2 2 0 0 1 2 2v11H4a2 2 0 0 1-2-2V6.5A3.5 3.5 0 0 1 5.5 3H17v3"/><path d="M15 11h6v5h-6a2.5 2.5 0 0 1 0-5Z"/></>,
  plus: <path d="M12 5v14M5 12h14"/>,
  arrowUpRight: <><path d="M7 17 17 7"/><path d="M8 7h9v9"/></>,
  arrowDownLeft: <><path d="m17 7-10 10"/><path d="M16 17H7V8"/></>,
  send: <><path d="m22 2-7 20-4-9-9-4z"/><path d="M22 2 11 13"/></>,
  copy: <><rect x="8" y="8" width="12" height="12" rx="2"/><path d="M16 8V6a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h2"/></>,
  check: <path d="m5 12 4 4L19 6"/>,
  clock: <><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></>,
  alert: <><path d="M10.3 3.5 2.6 18a2 2 0 0 0 1.8 3h15.2a2 2 0 0 0 1.8-3L13.7 3.5a2 2 0 0 0-3.4 0Z"/><path d="M12 9v4M12 17h.01"/></>,
  download: <><path d="M12 3v12"/><path d="m7 10 5 5 5-5"/><path d="M4 20h16"/></>,
  calendar: <><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M8 3v4M16 3v4M3 10h18"/></>,
  search: <><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></>,
  info: <><circle cx="12" cy="12" r="9"/><path d="M12 11v6M12 7h.01"/></>,
  building: <><path d="M3 21h18M5 21V9l7-5 7 5v12"/><path d="M9 21v-5h6v5M9 11h.01M15 11h.01"/></>,
  lightning: <path d="m13 2-9 12h7l-1 8 9-12h-7z"/>,
  home: <><path d="m3 11 9-8 9 8"/><path d="M5 10v11h14V10M9 21v-7h6v7"/></>,
  smartphone: <><rect x="6" y="2" width="12" height="20" rx="2"/><path d="M10 18h4"/></>,
  more: <><circle cx="5" cy="12" r="1" fill="currentColor" stroke="none"/><circle cx="12" cy="12" r="1" fill="currentColor" stroke="none"/><circle cx="19" cy="12" r="1" fill="currentColor" stroke="none"/></>,
  external: <><path d="M14 4h6v6M20 4l-9 9"/><path d="M19 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1h6"/></>,
  refresh: <><path d="M20 7v5h-5"/><path d="M4 17v-5h5"/><path d="M6.1 8a7 7 0 0 1 11.6-2.6L20 8M4 16l2.3 2.6A7 7 0 0 0 18 16"/></>,
  trash: <><path d="M4 7h16M9 7V4h6v3M7 7l1 14h8l1-14M10 11v6M14 11v6"/></>,
  key: <><circle cx="8" cy="15" r="4"/><path d="m11 12 9-9M16 7l2 2M18 5l2 2"/></>,
  verified: <><path d="m12 2 2.1 2.2 3-.4.8 2.9 2.7 1.4-1.4 2.7 1.4 2.7-2.7 1.4-.8 2.9-3-.4L12 22l-2.1-2.2-3 .4-.8-2.9-2.7-1.4 1.4-2.7-1.4-2.7 2.7-1.4.8-2.9 3 .4z"/><path d="m8.5 12 2.2 2.2 4.8-5"/></>,
  trend: <><path d="M3 19h18"/><path d="m5 15 4-4 3 3 6-7"/><path d="M15 7h3v3"/></>,
  receipt: <><path d="M5 3h14v19l-3-2-4 2-4-2-3 2z"/><path d="M8 8h8M8 12h8M8 16h5"/></>,
  support: <><circle cx="12" cy="12" r="9"/><path d="M3 13h3a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2H5M21 13h-3a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2h1M18 15v1a3 3 0 0 1-3 3h-2"/></>,
  filter: <path d="M3 5h18l-7 8v6l-4 2v-8z"/>,
};

export default function Icon({ name, size = 20, className = '', strokeWidth = 1.8, ...props }) {
  return (
    <svg
      className={`icon ${className}`}
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={strokeWidth}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      {...props}
    >
      {paths[name] || paths.info}
    </svg>
  );
}
