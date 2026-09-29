import React, { useMemo, useState } from 'react';
import BalanceCard from '../components/BalanceCard';
import Icon from '../components/Icon';
import { FieldError, Modal, SpinnerButton } from '../components/UI';
import { idempotencyKey } from '../api';
import { money } from '../utils';

export default function Transfer({ account, api, navigate, notify, refreshAccount }) {
  const [form, setForm] = useState({ destinationAccountNumber: '', amount: '', description: '' });
  const [errors, setErrors] = useState({});
  const [review, setReview] = useState(false);
  const [receipt, setReceipt] = useState(null);
  const [loading, setLoading] = useState(false);
  const numericAmount = Number(form.amount || 0);
  const remaining = useMemo(() => Number(account.balance) - numericAmount, [account.balance, numericAmount]);

  const validate = () => {
    const next = {};
    if (!/^\d{12}$/.test(form.destinationAccountNumber)) next.destinationAccountNumber = 'Enter a valid 12-digit account number.';
    if (!numericAmount || numericAmount < 0.01) next.amount = 'Enter an amount of at least 0.01.';
    if (numericAmount > Number(account.balance)) next.amount = 'This amount is more than your available balance.';
    setErrors(next); return Object.keys(next).length === 0;
  };

  const openReview = (event) => { event.preventDefault(); if (validate()) setReview(true); };
  const submit = async () => {
    setLoading(true);
    try {
      const result = await api('/api/transactions/transfers', {
        method: 'POST', headers: { 'Idempotency-Key': idempotencyKey('transfer') },
        body: { destinationAccountNumber: form.destinationAccountNumber, amount: Number(form.amount), description: form.description.trim() || 'Bank transfer' },
      });
      setReview(false); setReceipt(result); await refreshAccount();
      notify('Transfer complete', `${money(result.amount, result.currency)} was sent successfully.`);
      setForm({ destinationAccountNumber: '', amount: '', description: '' });
    } catch (error) { setReview(false); notify('Transfer not completed', error.message, 'error'); }
    finally { setLoading(false); }
  };

  if (account.kycStatus !== 'VERIFIED') return <div className="blocked-page page-enter"><span><Icon name="verified" size={38}/></span><p className="section-kicker">IDENTITY CHECK REQUIRED</p><h2>Verify your account before sending money</h2><p>Transfers become available after your digital KYC has been reviewed and approved. It helps us keep every account safe.</p><button className="primary-button" onClick={() => navigate('profile')}>Complete KYC <Icon name="arrowRight"/></button><button className="text-link" onClick={() => navigate('dashboard')}>Back to dashboard</button></div>;

  return (
    <div className="transfer-page page-enter">
      <div className="page-intro"><div><p className="section-kicker">INTRA-BANK TRANSFER</p><h2>Send money securely</h2><span>Fast, protected transfers between SecureBank accounts.</span></div><div className="steps"><span className="active"><i>1</i>Details</span><b/><span><i>2</i>Review</span><b/><span><i>3</i>Done</span></div></div>
      <div className="form-layout">
        <section className="panel form-panel">
          <div className="form-panel-head"><span><Icon name="send"/></span><div><h3>Transfer details</h3><p>Enter the recipient and amount to continue.</p></div></div>
          <form className="bank-form" onSubmit={openReview}>
            <div className="field-group"><label htmlFor="destination">Recipient account number</label><div className={`input-shell ${errors.destinationAccountNumber ? 'invalid' : ''}`}><Icon name="building"/><input id="destination" inputMode="numeric" maxLength={12} placeholder="12-digit SecureBank account" value={form.destinationAccountNumber} onChange={(e) => setForm({...form, destinationAccountNumber: e.target.value.replace(/\D/g, '')})}/></div><FieldError error={errors.destinationAccountNumber}/></div>
            <div className="field-group"><label htmlFor="transfer-amount">Amount</label><div className={`input-shell amount-input ${errors.amount ? 'invalid' : ''}`}><span>{account.currency === 'INR' ? '₹' : account.currency}</span><input id="transfer-amount" type="number" min="0.01" step="0.01" placeholder="0.00" value={form.amount} onChange={(e) => setForm({...form, amount: e.target.value})}/></div><div className="field-help"><FieldError error={errors.amount}/><small>Available: <strong>{money(account.balance, account.currency)}</strong></small></div></div>
            <div className="field-group"><label htmlFor="transfer-description">Note <small>Optional</small></label><div className="input-shell"><Icon name="document"/><input id="transfer-description" maxLength={255} placeholder="What is this transfer for?" value={form.description} onChange={(e) => setForm({...form, description: e.target.value})}/></div></div>
            <div className="info-callout"><Icon name="shield"/><div><strong>Protected transfer</strong><p>Every transfer is checked by real-time fraud monitoring and committed atomically.</p></div></div>
            <button className="primary-button wide" type="submit">Review transfer <Icon name="arrowRight"/></button>
          </form>
        </section>
        <aside className="transfer-aside"><BalanceCard account={account} compact onNavigate={navigate} onCopy={() => {}}/><section className="panel transfer-summary"><h3>Before you send</h3><ul><li><span><Icon name="check"/></span>Check the recipient account number</li><li><span><Icon name="check"/></span>Transfers cannot be cancelled after completion</li><li><span><Icon name="check"/></span>Your receipt is generated instantly</li></ul><div className="limit-row"><span>Fraud protection</span><strong>Always active</strong></div></section></aside>
      </div>

      {review && <Modal title="Review your transfer" description="Please confirm these details before sending." onClose={() => !loading && setReview(false)}><div className="review-card"><div className="review-route"><span className="avatar account-avatar">{account.fullName[0]}</span><i/><span className="avatar recipient-avatar"><Icon name="building"/></span></div><div className="review-amount"><small>YOU ARE SENDING</small><strong>{money(numericAmount, account.currency)}</strong></div><dl><div><dt>From</dt><dd>{account.fullName}<small>•••• {account.accountNumber.slice(-4)}</small></dd></div><div><dt>To account</dt><dd>{form.destinationAccountNumber}</dd></div><div><dt>Note</dt><dd>{form.description || 'Bank transfer'}</dd></div><div><dt>Balance after</dt><dd>{money(remaining, account.currency)}</dd></div></dl></div><div className="modal-actions"><button className="secondary-button" onClick={() => setReview(false)} disabled={loading}>Go back</button><SpinnerButton loading={loading} className="primary-button" onClick={submit}>Confirm & send <Icon name="send"/></SpinnerButton></div></Modal>}

      {receipt && <Modal title="Transfer successful" description="Your money has been moved safely." onClose={() => setReceipt(null)} size="small"><div className="success-receipt"><span className="success-check"><Icon name="check" size={30}/></span><p>AMOUNT SENT</p><h3>{money(receipt.amount, receipt.currency)}</h3><div><span>Reference</span><strong>{receipt.reference}</strong></div><div><span>Status</span><strong className="green-text">{receipt.status}</strong></div><div><span>Available balance</span><strong>{money(receipt.balanceAfter, receipt.currency)}</strong></div></div><button className="primary-button wide" onClick={() => setReceipt(null)}>Done</button></Modal>}
    </div>
  );
}
