import React, { useEffect, useState } from 'react';
import Icon from '../components/Icon';
import { EmptyState, PageLoader, StatusBadge } from '../components/UI';
import { dateTime } from '../utils';

const faqs = [
  ['Why is my transfer blocked?', 'SecureBank evaluates amount, daily volume and recent transfer velocity in real time. A suspicious transfer is stopped before funds move and reviewed for your safety.'],
  ['What happens when an external payment fails?', 'The payment workflow automatically posts a compensating credit reversal. If manual attention is required, a support ticket is created for you.'],
  ['How do I complete digital KYC?', 'Open Profile & KYC, choose an accepted document, enter its number, provide consent and submit. An administrator will review the request.'],
  ['Where can I find my statement?', 'Open Statements, choose a date range, and download the CSV. Every line comes directly from your immutable account ledger.'],
];

export default function Support({ api, navigate, notify }) {
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [openFaq, setOpenFaq] = useState(0);
  useEffect(() => { api('/api/help/tickets').then(setTickets).catch((error) => notify('Could not load support tickets', error.message, 'error')).finally(() => setLoading(false)); }, []); // eslint-disable-line react-hooks/exhaustive-deps
  if (loading) return <PageLoader label="Opening support centre"/>;
  return (
    <div className="support-page page-enter">
      <section className="support-hero"><div><p className="section-kicker light">SECUREBANK CARE</p><h2>How can we help?</h2><p>Get clear answers, track automatically generated tickets, or reach our support team.</p><div className="support-search"><Icon name="search"/><input aria-label="Search help" placeholder="Search help topics" onChange={(event) => { if (event.target.value.length > 2) setOpenFaq(faqs.findIndex((faq) => faq.join(' ').toLowerCase().includes(event.target.value.toLowerCase()))); }}/></div></div><span className="support-illustration"><Icon name="support" size={58}/><i/><b/></span></section>
      <div className="support-options"><a href="mailto:support@securebank.example"><span><Icon name="mail"/></span><div><strong>Email support</strong><small>Response within one business day</small></div><Icon name="arrowUpRight"/></a><button onClick={() => notify('24/7 fraud line', 'Call your configured banking support number immediately for suspicious activity.')}><span><Icon name="phone"/></span><div><strong>Fraud assistance</strong><small>Guidance for suspicious activity</small></div><Icon name="chevronRight"/></button><button onClick={() => navigate('notifications')}><span><Icon name="bell"/></span><div><strong>Security alerts</strong><small>Review recent account activity</small></div><Icon name="chevronRight"/></button></div>
      <div className="support-grid">
        <section className="panel tickets-panel"><div className="panel-head"><div><p className="section-kicker">CASE TRACKING</p><h3>Your support tickets</h3></div><span className="ticket-count">{tickets.length}</span></div>{tickets.length === 0 ? <EmptyState icon="support" title="No open support tickets" description="If a transfer fails, SecureBank automatically creates a case here so you never need to repeat the details."/> : <div className="ticket-list">{tickets.map((ticket) => <article key={ticket.ticketNumber}><span className="ticket-icon"><Icon name="help"/></span><div><div><h3>{ticket.subject}</h3><StatusBadge status={ticket.status}/></div><p>{ticket.details}</p><small><code>{ticket.ticketNumber}</code>{ticket.transactionReference && <> • Transfer {ticket.transactionReference}</>} • {dateTime(ticket.createdAt)}</small></div></article>)}</div>}</section>
        <section className="panel faq-panel"><div className="panel-head"><div><p className="section-kicker">QUICK ANSWERS</p><h3>Frequently asked questions</h3></div><Icon name="help"/></div><div className="faq-list">{faqs.map(([question, answer], index) => <article className={openFaq === index ? 'open' : ''} key={question}><button onClick={() => setOpenFaq(openFaq === index ? -1 : index)}><span>{question}</span><Icon name={openFaq === index ? 'close' : 'plus'} size={18}/></button>{openFaq === index && <p>{answer}</p>}</article>)}</div></section>
      </div>
      <div className="safety-strip"><span><Icon name="shield"/></span><div><strong>Never share your password or OTP</strong><p>SecureBank staff will never ask for your full password, one-time code, or JWT token.</p></div><button className="text-link" onClick={() => navigate('notifications')}>Review alerts <Icon name="arrowRight"/></button></div>
    </div>
  );
}
