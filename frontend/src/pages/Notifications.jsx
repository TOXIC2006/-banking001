import React, { useEffect, useMemo, useState } from 'react';
import Icon from '../components/Icon';
import { EmptyState, Modal, PageLoader, SpinnerButton, StatusBadge } from '../components/UI';
import { dateTime, localDateTimeValue, money, titleCase } from '../utils';

const notificationIcon = { OTP: 'key', LOGIN_ALERT: 'shield', TRANSACTION_RECEIPT: 'transfer', PAYMENT_RECEIPT: 'receipt', BILL_REMINDER: 'calendar' };

export default function Notifications({ account, api, notify }) {
  const [notifications, setNotifications] = useState([]);
  const [reminders, setReminders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('all');
  const [otpOpen, setOtpOpen] = useState(false);
  const [reminderOpen, setReminderOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [otp, setOtp] = useState({ channel: 'EMAIL', purpose: 'LOGIN_VERIFICATION', requested: false, code: '' });
  const due = new Date(Date.now() + 7 * 86400000); const remind = new Date(Date.now() + 6 * 86400000);
  const [reminder, setReminder] = useState({ biller: '', amount: '', currency: account.currency, dueAt: localDateTimeValue(due), remindAt: localDateTimeValue(remind) });

  const load = async () => {
    const [notes, bills] = await Promise.allSettled([api('/api/notifications'), api('/api/notifications/reminders')]);
    setNotifications(notes.status === 'fulfilled' ? notes.value : []); setReminders(bills.status === 'fulfilled' ? bills.value : []); setLoading(false);
  };
  useEffect(() => { load(); }, []); // eslint-disable-line react-hooks/exhaustive-deps
  const visible = useMemo(() => tab === 'security' ? notifications.filter((item) => ['OTP', 'LOGIN_ALERT'].includes(item.type)) : notifications, [notifications, tab]);

  const requestOtp = async (event) => {
    event.preventDefault(); setSubmitting(true);
    try { await api('/api/notifications/otp', { method: 'POST', body: { channel: otp.channel, purpose: otp.purpose } }); setOtp({...otp, requested: true}); notify('OTP queued', `A verification code was sent by ${otp.channel.toLowerCase()}.`); await load(); }
    catch (error) { notify('Could not send OTP', error.message, 'error'); }
    finally { setSubmitting(false); }
  };
  const verifyOtp = async (event) => {
    event.preventDefault(); setSubmitting(true);
    try { const result = await api('/api/notifications/otp/verify', { method: 'POST', body: { purpose: otp.purpose, code: otp.code } }); if (result.valid) { notify('OTP verified', 'Your verification code was accepted.'); setOtpOpen(false); setOtp({ channel: 'EMAIL', purpose: 'LOGIN_VERIFICATION', requested: false, code: '' }); } else notify('Invalid code', 'That code is invalid or has expired.', 'error'); }
    catch (error) { notify('Verification failed', error.message, 'error'); }
    finally { setSubmitting(false); }
  };
  const createReminder = async (event) => {
    event.preventDefault(); setSubmitting(true);
    try { await api('/api/notifications/reminders', { method: 'POST', body: { ...reminder, amount: Number(reminder.amount), currency: reminder.currency.toUpperCase(), dueAt: new Date(reminder.dueAt).toISOString(), remindAt: new Date(reminder.remindAt).toISOString() } }); notify('Reminder created', `We will remind you before ${reminder.biller} is due.`); setReminderOpen(false); await load(); }
    catch (error) { notify('Could not create reminder', error.message, 'error'); }
    finally { setSubmitting(false); }
  };
  const removeReminder = async (id) => {
    try { await api(`/api/notifications/reminders/${id}`, { method: 'DELETE' }); setReminders((items) => items.filter((item) => item.id !== id)); notify('Reminder removed', 'The bill reminder has been cancelled.'); }
    catch (error) { notify('Could not remove reminder', error.message, 'error'); }
  };

  if (loading) return <PageLoader label="Loading notifications"/>;
  return (
    <div className="notifications-page page-enter">
      <div className="page-intro"><div><p className="section-kicker">ALERT CENTRE</p><h2>Stay informed, stay secure</h2><span>Receipts, security alerts and reminders in one place.</span></div><div className="intro-actions"><button className="secondary-button" onClick={() => setOtpOpen(true)}><Icon name="key"/>Request OTP</button><button className="primary-button" onClick={() => setReminderOpen(true)}><Icon name="plus"/>Bill reminder</button></div></div>
      <div className="notification-tabs"><button className={tab === 'all' ? 'active' : ''} onClick={() => setTab('all')}>All alerts <span>{notifications.length}</span></button><button className={tab === 'reminders' ? 'active' : ''} onClick={() => setTab('reminders')}>Bill reminders <span>{reminders.length}</span></button><button className={tab === 'security' ? 'active' : ''} onClick={() => setTab('security')}>Security</button></div>

      {tab === 'reminders' ? <section className="panel notification-list-panel"><div className="list-section-head"><div><h3>Scheduled reminders</h3><p>We’ll alert you at your chosen time.</p></div></div>{reminders.length === 0 ? <EmptyState icon="calendar" title="No reminders scheduled" description="Create a reminder and never miss another due date." action={<button className="secondary-button" onClick={() => setReminderOpen(true)}>Create reminder</button>}/> : <div className="reminder-list">{reminders.map((item) => <article className="reminder-item" key={item.id}><span className="reminder-date"><b>{new Date(item.dueAt).getDate()}</b><small>{new Date(item.dueAt).toLocaleDateString('en-IN', {month:'short'}).toUpperCase()}</small></span><div><h3>{item.biller}</h3><p>Due {dateTime(item.dueAt)} • Reminder {dateTime(item.remindAt)}</p></div><strong>{money(item.amount, item.currency)}</strong><StatusBadge status={item.dispatched ? 'SENT' : 'SCHEDULED'}/><button className="icon-button danger" onClick={() => removeReminder(item.id)} aria-label="Delete reminder"><Icon name="trash"/></button></article>)}</div>}</section> : <section className="panel notification-list-panel"><div className="list-section-head"><div><h3>{tab === 'security' ? 'Security activity' : 'Recent notifications'}</h3><p>Your latest account updates and delivery status.</p></div><span className="live-label"><i/>LIVE</span></div>{visible.length === 0 ? <EmptyState icon="bell" title="You’re all caught up" description={tab === 'security' ? 'Security and login alerts will appear here.' : 'New alerts and receipts will show up here.'}/> : <div className="notification-list">{visible.map((item) => <article className="notification-item" key={item.id}><span className={`notification-type type-${item.type?.toLowerCase()}`}><Icon name={notificationIcon[item.type] || 'bell'}/></span><div><div><h3>{item.subject}</h3><StatusBadge status={item.status}/></div><p>{item.content}</p><small>{dateTime(item.createdAt)} • {titleCase(item.channel)} to {item.maskedDestination}</small></div></article>)}</div>}</section>}

      {otpOpen && <Modal title={otp.requested ? 'Verify your code' : 'Request an OTP'} description={otp.requested ? 'Enter the 6-digit code sent to your selected channel.' : 'Choose where we should send your one-time code.'} onClose={() => setOtpOpen(false)} size="small"><form className="bank-form" onSubmit={otp.requested ? verifyOtp : requestOtp}>{!otp.requested ? <><div className="channel-choice"><button type="button" className={otp.channel === 'EMAIL' ? 'active' : ''} onClick={() => setOtp({...otp, channel:'EMAIL'})}><Icon name="mail"/><strong>Email</strong><small>{account.email.replace(/(.{2}).+(@.+)/, '$1***$2')}</small></button><button type="button" className={otp.channel === 'SMS' ? 'active' : ''} onClick={() => setOtp({...otp, channel:'SMS'})}><Icon name="smartphone"/><strong>SMS</strong><small>•••• {account.phone.slice(-4)}</small></button></div><div className="field-group"><label htmlFor="otp-purpose">Verification purpose</label><div className="input-shell"><Icon name="shield"/><select id="otp-purpose" value={otp.purpose} onChange={(e) => setOtp({...otp,purpose:e.target.value})}><option value="LOGIN_VERIFICATION">Login verification</option><option value="PROFILE_UPDATE">Profile update</option><option value="PAYMENT_VERIFICATION">Payment verification</option></select></div></div></> : <div className="otp-entry"><Icon name="key" size={28}/><label htmlFor="otp-code">6-digit verification code</label><input id="otp-code" inputMode="numeric" pattern="[0-9]{6}" maxLength={6} placeholder="000000" value={otp.code} onChange={(e) => setOtp({...otp,code:e.target.value.replace(/\D/g,'')})} autoFocus required/><button className="text-link" type="button" onClick={() => setOtp({...otp,requested:false,code:''})}>Send another code</button></div>}<SpinnerButton loading={submitting} className="primary-button wide" type="submit">{otp.requested ? 'Verify code' : 'Send secure code'} <Icon name="arrowRight"/></SpinnerButton></form></Modal>}

      {reminderOpen && <Modal title="Create bill reminder" description="Choose when SecureBank should remind you." onClose={() => setReminderOpen(false)}><form className="bank-form" onSubmit={createReminder}><div className="field-group"><label htmlFor="biller">Biller name</label><div className="input-shell"><Icon name="home"/><input id="biller" maxLength={160} placeholder="Electricity, broadband, rent…" value={reminder.biller} onChange={(e) => setReminder({...reminder,biller:e.target.value})} required/></div></div><div className="split-fields"><div className="field-group"><label htmlFor="bill-amount">Amount</label><div className="input-shell"><Icon name="wallet"/><input id="bill-amount" type="number" min="0.01" step="0.01" value={reminder.amount} onChange={(e) => setReminder({...reminder,amount:e.target.value})} required/></div></div><div className="field-group"><label htmlFor="bill-currency">Currency</label><div className="input-shell"><Icon name="card"/><input id="bill-currency" maxLength={3} value={reminder.currency} onChange={(e) => setReminder({...reminder,currency:e.target.value.toUpperCase()})} required/></div></div></div><div className="split-fields"><div className="field-group"><label htmlFor="due-at">Due at</label><div className="input-shell"><Icon name="calendar"/><input id="due-at" type="datetime-local" value={reminder.dueAt} onChange={(e) => setReminder({...reminder,dueAt:e.target.value})} required/></div></div><div className="field-group"><label htmlFor="remind-at">Remind me at</label><div className="input-shell"><Icon name="bell"/><input id="remind-at" type="datetime-local" value={reminder.remindAt} onChange={(e) => setReminder({...reminder,remindAt:e.target.value})} required/></div></div></div><SpinnerButton loading={submitting} className="primary-button wide" type="submit">Create reminder <Icon name="check"/></SpinnerButton></form></Modal>}
    </div>
  );
}
