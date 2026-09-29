import React, { useEffect, useMemo, useState } from 'react';
import BalanceCard from '../components/BalanceCard';
import Icon from '../components/Icon';
import { EmptyState, PageLoader, StatusBadge } from '../components/UI';
import { dateTime, firstName, greeting, money, shortMoney, titleCase } from '../utils';

export default function Dashboard({ account, api, navigate, notify }) {
  const [data, setData] = useState({ payments: [], notifications: [], reminders: [] });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    Promise.allSettled([api('/api/payments'), api('/api/notifications'), api('/api/notifications/reminders')])
      .then(([payments, notifications, reminders]) => {
        if (!active) return;
        setData({
          payments: payments.status === 'fulfilled' ? payments.value : [],
          notifications: notifications.status === 'fulfilled' ? notifications.value : [],
          reminders: reminders.status === 'fulfilled' ? reminders.value : [],
        });
      }).finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [api]);

  const completedTotal = useMemo(() => data.payments.filter((p) => p.status === 'COMPLETED').reduce((sum, p) => sum + Number(p.amount), 0), [data.payments]);
  const chart = useMemo(() => {
    const points = data.payments.slice(0, 6).reverse();
    const max = Math.max(...points.map((p) => Number(p.amount)), 1);
    return points.map((p) => ({ label: new Date(p.createdAt).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' }), value: Number(p.amount), height: Math.max(14, (Number(p.amount) / max) * 100) }));
  }, [data.payments]);
  const pendingNotifications = data.notifications.filter((n) => n.status !== 'SENT').length;

  if (loading) return <PageLoader />;
  const copy = async (value) => { await navigator.clipboard.writeText(value); notify('Copied', 'Account number copied to your clipboard.'); };

  return (
    <div className="dashboard page-enter">
      <div className="welcome-row"><div><p>{greeting()},</p><h2>{firstName(account.fullName)} <span>👋</span></h2><small>Here is what is happening with your money today.</small></div><div className="today-chip"><Icon name="calendar"/><span><small>TODAY</small>{new Date().toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}</span></div></div>

      {account.kycStatus !== 'VERIFIED' && <section className="kyc-banner"><span className="kyc-banner-icon"><Icon name="verified"/></span><div><strong>{account.kycStatus === 'UNDER_REVIEW' ? 'Your KYC is under review' : 'Complete your KYC to unlock transfers'}</strong><p>{account.kycStatus === 'UNDER_REVIEW' ? 'We will update your account as soon as verification is complete.' : 'Secure identity verification keeps your account and every transaction protected.'}</p></div><button className="secondary-button" onClick={() => navigate('profile')}>{account.kycStatus === 'UNDER_REVIEW' ? 'View status' : 'Verify now'}<Icon name="arrowRight" size={17}/></button></section>}

      <div className="dashboard-grid top-grid">
        <BalanceCard account={account} onNavigate={navigate} onCopy={copy}/>
        <section className="panel account-pulse">
          <div className="panel-head"><div><p className="section-kicker">THIS MONTH</p><h3>Account pulse</h3></div><span className="soft-icon"><Icon name="trend"/></span></div>
          <div className="pulse-stats"><div><span className="metric-icon teal"><Icon name="arrowUpRight"/></span><span><small>External payments</small><strong>{shortMoney(completedTotal, account.currency)}</strong></span></div><div><span className="metric-icon green"><Icon name="receipt"/></span><span><small>Payments made</small><strong>{data.payments.length}</strong></span></div><div><span className="metric-icon sky"><Icon name="calendar"/></span><span><small>Bill reminders</small><strong>{data.reminders.filter((item) => !item.dispatched).length}</strong></span></div></div>
          <div className="pulse-note"><span><Icon name="shield"/></span><div><strong>Everything looks good</strong><p>No unusual activity detected on your account.</p></div></div>
        </section>
      </div>

      <div className="stat-strip">
        <button onClick={() => navigate('transfer')}><span className="stat-icon navy"><Icon name="send"/></span><span><small>Quick action</small><strong>Send money</strong></span><Icon name="chevronRight"/></button>
        <button onClick={() => navigate('payments')}><span className="stat-icon teal"><Icon name="lightning"/></span><span><small>IMPS, NEFT & utility</small><strong>Make a payment</strong></span><Icon name="chevronRight"/></button>
        <button onClick={() => navigate('statements')}><span className="stat-icon green"><Icon name="download"/></span><span><small>Last 30 days</small><strong>Get statement</strong></span><Icon name="chevronRight"/></button>
        <button onClick={() => navigate('notifications')}><span className="stat-icon sky"><Icon name="bell"/></span><span><small>{pendingNotifications ? `${pendingNotifications} need attention` : 'All caught up'}</small><strong>Notifications</strong></span><Icon name="chevronRight"/></button>
      </div>

      <div className="dashboard-grid lower-grid">
        <section className="panel recent-panel">
          <div className="panel-head"><div><p className="section-kicker">MONEY MOVEMENT</p><h3>Recent payments</h3></div><button className="text-link" onClick={() => navigate('payments')}>View all <Icon name="arrowRight" size={16}/></button></div>
          {data.payments.length === 0 ? <EmptyState icon="receipt" title="No payments yet" description="Your IMPS, NEFT and utility payment activity will appear here." action={<button className="secondary-button" onClick={() => navigate('payments')}>Make first payment</button>}/> : <div className="activity-list">{data.payments.slice(0, 5).map((payment) => <div className="activity-row" key={payment.reference}><span className={`activity-icon rail-${payment.rail?.toLowerCase()}`}><Icon name={payment.rail === 'UTILITY' ? 'lightning' : payment.rail === 'PAYMENT_GATEWAY' ? 'card' : 'building'}/></span><div className="activity-main"><strong>{payment.beneficiary}</strong><small>{titleCase(payment.rail)} • {dateTime(payment.createdAt)}</small></div><div className="activity-amount"><strong>-{money(payment.amount, payment.currency || account.currency)}</strong><StatusBadge status={payment.status}/></div></div>)}</div>}
        </section>

        <section className="panel spending-panel">
          <div className="panel-head"><div><p className="section-kicker">PAYMENT TREND</p><h3>Recent outgoing</h3></div><span className="period-chip">Latest 6</span></div>
          {chart.length === 0 ? <div className="chart-empty"><span><Icon name="trend" size={26}/></span><p>Make a payment to start seeing your trend.</p></div> : <><div className="bar-chart">{chart.map((point, index) => <div className="bar-column" key={`${point.label}-${index}`}><span className="bar-value">{shortMoney(point.value, account.currency)}</span><div className="bar-track"><i style={{height: `${point.height}%`}}/></div><small>{point.label}</small></div>)}</div><div className="chart-legend"><span><i/>External payment amount</span><strong>{money(completedTotal, account.currency)} total</strong></div></>}
        </section>
      </div>
    </div>
  );
}
