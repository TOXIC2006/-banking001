import React, { useState } from 'react';
import Icon from '../components/Icon';
import { SpinnerButton } from '../components/UI';
import { dateOnly } from '../utils';

function isoDate(date) { return date.toISOString().slice(0, 10); }

export default function Statements({ account, api, notify }) {
  const now = new Date();
  const monthAgo = new Date(now); monthAgo.setDate(now.getDate() - 30);
  const [range, setRange] = useState({ from: isoDate(monthAgo), to: isoDate(now) });
  const [loading, setLoading] = useState(false);

  const chooseRange = (days) => {
    const end = new Date(); const start = new Date(); start.setDate(end.getDate() - days);
    setRange({ from: isoDate(start), to: isoDate(end) });
  };

  const download = async () => {
    if (!range.from || !range.to || new Date(range.from) > new Date(range.to)) { notify('Invalid date range', 'Choose a start date before the end date.', 'error'); return; }
    setLoading(true);
    try {
      const from = new Date(`${range.from}T00:00:00`).toISOString();
      const to = new Date(`${range.to}T23:59:59`).toISOString();
      const blob = await api(`/api/accounts/me/statements?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`, { responseType: 'blob' });
      const url = URL.createObjectURL(blob); const link = document.createElement('a');
      link.href = url; link.download = `SecureBank-statement-${range.from}-to-${range.to}.csv`; link.click(); URL.revokeObjectURL(url);
      notify('Statement ready', 'Your CSV statement has been downloaded.');
    } catch (error) { notify('Statement unavailable', error.message, 'error'); }
    finally { setLoading(false); }
  };

  return (
    <div className="statements-page page-enter">
      <div className="page-intro"><div><p className="section-kicker">E-STATEMENTS</p><h2>Your records, ready when you are</h2><span>Generate a secure account statement for any date range.</span></div><span className="account-pill"><Icon name="wallet"/><span><small>ACCOUNT</small>•••• {account.accountNumber.slice(-4)}</span></span></div>
      <div className="statement-layout">
        <section className="panel statement-builder"><div className="statement-hero"><span><Icon name="document" size={28}/></span><div><p>SECUREBANK E-STATEMENT</p><h3>Account activity report</h3><small>Generated directly from your immutable ledger.</small></div></div><div className="statement-account"><div><small>ACCOUNT HOLDER</small><strong>{account.fullName}</strong></div><div><small>ACCOUNT NUMBER</small><strong>{account.accountNumber}</strong></div><div><small>CURRENCY</small><strong>{account.currency}</strong></div><div><small>ACCOUNT STATUS</small><strong className="green-text">Active</strong></div></div><div className="statement-range"><h3>Select a period</h3><div className="range-pills"><button onClick={() => chooseRange(7)}>Last 7 days</button><button onClick={() => chooseRange(30)}>Last 30 days</button><button onClick={() => chooseRange(90)}>Last 90 days</button></div><div className="date-fields"><div><label htmlFor="statement-from">From date</label><div className="input-shell"><Icon name="calendar"/><input id="statement-from" type="date" value={range.from} max={range.to} onChange={(e) => setRange({...range, from: e.target.value})}/></div></div><span><Icon name="arrowRight"/></span><div><label htmlFor="statement-to">To date</label><div className="input-shell"><Icon name="calendar"/><input id="statement-to" type="date" value={range.to} min={range.from} max={isoDate(now)} onChange={(e) => setRange({...range, to: e.target.value})}/></div></div></div></div><div className="statement-download-row"><div><Icon name="info"/><p><strong>CSV format</strong><small>Easy to open in Excel, Sheets, or accounting software.</small></p></div><SpinnerButton loading={loading} className="primary-button" onClick={download}><Icon name="download"/>Download statement</SpinnerButton></div></section>
        <aside className="statement-aside"><section className="panel"><span className="aside-feature-icon"><Icon name="shield"/></span><h3>Verified records</h3><p>Each line comes from SecureBank’s transaction ledger, keeping your records consistent and dependable.</p></section><section className="panel statement-preview"><div className="preview-lines"><i/><i/><i/><i/><i/></div><span className="preview-badge"><Icon name="check"/> READY</span><h3>Your selected period</h3><p>{dateOnly(`${range.from}T00:00:00`)} — {dateOnly(`${range.to}T00:00:00`)}</p></section><div className="privacy-note"><Icon name="lock"/><span><strong>Private by design</strong><small>Your statement is created only after authentication and is never shared.</small></span></div></aside>
      </div>
    </div>
  );
}
