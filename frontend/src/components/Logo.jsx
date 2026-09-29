import React from 'react';

export default function Logo({ light = false, compact = false }) {
  return (
    <div className={`brand ${light ? 'brand-light' : ''}`} aria-label="SecureBank">
      <span className="brand-mark" aria-hidden="true">
        <svg viewBox="0 0 40 40">
          <path d="M20 3.5 34 9v10.2c0 8.4-5.8 14.4-14 17.3-8.2-2.9-14-8.9-14-17.3V9l14-5.5Z" fill="currentColor" />
          <path d="m14 20 4 4 8-9" fill="none" stroke={light ? '#082F49' : '#F0FDFF'} strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </span>
      {!compact && <span>Secure<strong>Bank</strong></span>}
    </div>
  );
}
