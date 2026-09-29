import React, { useEffect, useMemo, useState } from 'react';
import Icon from '../components/Icon';
import { EmptyState, PageLoader, SpinnerButton, StatusBadge } from '../components/UI';
import { idempotencyKey } from '../api';
import { dateTime, money, titleCase } from '../utils';

const rails = [
  { id: 'IMPS', label: 'IMPS', note: 'Instant bank transfer', icon: 'lightning' },
  { id: 'NEFT', label: 'NEFT', note: 'Scheduled bank transfer', icon: 'building' },
  { id: 'UTILITY', label: 'Utility', note: 'Electricity, water & more', icon: 'home' },
  { id: 'PAYMENT_GATEWAY', label: 'Gateway', note: 'Merchant payment', icon: 'card' },
];

export default function Payments({ account, api, navigate, notify, refreshAccount }) {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [tab, setTab] = useState('new');
  const [filter, setFilter] = useState('ALL');
  const [form, setForm] = useState({ rail: 'IMPS', beneficiary: '', routingCode: '', amount: '' });

  const load = () => api('/api/payments').then(setPayments).catch((error) => notify('Could not load payments', error.message, 'error')).finally(() => setLoading(false));
  useEffect(() => { load(); }, []); // eslint-disable-line react-hooks/exhaustive-deps
  const visible = useMemo(() => filter === 'ALL' ? payments : payments.filter((payment) => payment.status === filter), [payments, filter]);
  const requiresRouting = form.rail === 'IMPS' || form.rail === 'NEFT';

  const submit = async (event) => {
    event.preventDefault();
    if (account.kycStatus !== 'VERIFIED') { notify('KYC required', 'Verify your identity before making external payments.', 'error'); return; }
    setSending(true);
    try {
      const result = await api('/api/payments', {
        method: 'POST', headers: { 'Idempotency-Key': idempotencyKey('payment') },
        body: { ...form, amount: Number(form.amount), routingCode: form.routingCode || null },
      });
      notify('Payment submitted', `${money(result.amount, result.currency || account.currency)} was processed successfully.`);
      setForm({ rail: 'IMPS', beneficiary: '', routingCode: '', amount: '' });
      await Promise.all([load(), refreshAccount()]); setTab('history');
    } catch (error) { notify('Payment not completed', error.message, 'error'); await load(); }
    finally { setSending(false); }
  };

  if (loading) return <PageLoader label="Loading payments"/>;
  return (
    <div className="payments-page page-enter">
      <div className="page-intro"><div><p className="section-kicker">PAYMENT CENTRE</p><h2>Move money your way</h2><span>IMPS, NEFT, utility and merchant payments in one place.</span></div><div className="segmented-tabs"><button className={tab === 'new' ? 'active' : ''} onClick={() => setTab('new')}><Icon name="plus"/>New payment</button><button className={tab === 'history' ? 'active' : ''} onClick={() => setTab('history')}><Icon name="clock"/>Payment history</button></div></div>

      {tab === 'new' ? <div className="payment-layout">
        <section className="payment-methods"><h3>Choose payment type</h3><div className="rail-grid">{rails.map((rail) => <button key={rail.id} className={form.rail === rail.id ? 'active' : ''} onClick={() => setForm({...form, rail: rail.id, routingCode: ''})}><span><Icon name={rail.icon}/></span><strong>{rail.label}</strong><small>{rail.note}</small><i><Icon name="check" size={13}/></i></button>)}</div><section className="panel payment-benefits"><div><span><Icon name="lightning"/></span><strong>Fast settlement</strong><small>Built for real-time payment rails.</small></div><div><span><Icon name="shield"/></span><strong>Fraud checked</strong><small>Every payment is evaluated before debit.</small></div><div><span><Icon name="refresh"/></span><strong>Automatic reversal</strong><small>Failed settlement triggers fund recovery.</small></div></section></section>
        <section className="panel payment-form-card"><div className="form-panel-head"><span><Icon name={rails.find((r) => r.id === form.rail)?.icon}/></span><div><h3>{titleCase(form.rail)} payment</h3><p>Enter settlement details below.</p></div></div>{account.kycStatus !== 'VERIFIED' && <div className="mini-warning"><Icon name="alert"/><span>KYC verification is required. <button onClick={() => navigate('profile')}>Complete KYC</button></span></div>}<form className="bank-form" onSubmit={submit}><div className="field-group"><label htmlFor="beneficiary">{form.rail === 'UTILITY' ? 'Biller / consumer ID' : 'Beneficiary'}</label><div className="input-shell"><Icon name={form.rail === 'UTILITY' ? 'home' : 'user'}/><input id="beneficiary" maxLength={160} placeholder={form.rail === 'UTILITY' ? 'Biller name or consumer number' : 'Name or beneficiary account'} value={form.beneficiary} onChange={(e) => setForm({...form, beneficiary: e.target.value})} required/></div></div>{requiresRouting && <div className="field-group"><label htmlFor="routing">Routing / IFSC code</label><div className="input-shell"><Icon name="building"/><input id="routing" maxLength={40} placeholder="e.g. SECB0001234" value={form.routingCode} onChange={(e) => setForm({...form, routingCode: e.target.value.toUpperCase()})} required/></div></div>}<div className="field-group"><label htmlFor="payment-amount">Amount</label><div className="input-shell amount-input"><span>{account.currency === 'INR' ? '₹' : account.currency}</span><input id="payment-amount" type="number" min="0.01" step="0.01" max={account.balance} placeholder="0.00" value={form.amount} onChange={(e) => setForm({...form, amount: e.target.value})} required/></div><div className="field-help"><span/><small>Available: <strong>{money(account.balance, account.currency)}</strong></small></div></div><div className="payment-total"><span>Payment amount</span><strong>{money(Number(form.amount || 0), account.currency)}</strong><small>No SecureBank processing fee</small></div><SpinnerButton loading={sending} className="primary-button wide" type="submit" disabled={account.kycStatus !== 'VERIFIED'}>Continue securely <Icon name="arrowRight"/></SpinnerButton></form></section>
      </div> : <section className="panel history-card"><div className="history-toolbar"><div><p className="section-kicker">ALL ACTIVITY</p><h3>Payment history</h3></div><div className="filter-pills">{['ALL', 'COMPLETED', 'REFUNDED', 'FAILED'].map((value) => <button key={value} className={filter === value ? 'active' : ''} onClick={() => setFilter(value)}>{titleCase(value)}</button>)}</div></div>{visible.length === 0 ? <EmptyState icon="receipt" title={payments.length ? 'No matching payments' : 'No payment history yet'} description={payments.length ? 'Try another status filter.' : 'Your completed and pending payments will appear here.'} action={!payments.length && <button className="secondary-button" onClick={() => setTab('new')}>Make a payment</button>}/> : <div className="payment-table-wrap"><table className="data-table"><thead><tr><th>Beneficiary</th><th>Type</th><th>Reference</th><th>Date</th><th>Status</th><th className="align-right">Amount</th></tr></thead><tbody>{visible.map((payment) => <tr key={payment.reference}><td><div className="table-beneficiary"><span><Icon name={payment.rail === 'UTILITY' ? 'home' : payment.rail === 'PAYMENT_GATEWAY' ? 'card' : 'building'}/></span><strong>{payment.beneficiary}</strong></div></td><td>{titleCase(payment.rail)}</td><td><code>{payment.reference}</code></td><td>{dateTime(payment.createdAt)}</td><td><StatusBadge status={payment.status}/></td><td className="align-right amount-cell">-{money(payment.amount, payment.currency || account.currency)}</td></tr>)}</tbody></table></div>}</section>}
    </div>
  );
}
