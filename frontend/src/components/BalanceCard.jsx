import React, { useState } from 'react';
import Icon from './Icon';
import { maskAccount, money } from '../utils';

export default function BalanceCard({ account, onNavigate, onCopy, compact = false }) {
  const [visible, setVisible] = useState(true);
  return (
    <section className={`balance-card ${compact ? 'compact' : ''}`}>
      <div className="balance-orb one"/><div className="balance-orb two"/>
      <div className="balance-top"><span className="balance-label">AVAILABLE BALANCE</span><button className="ghost-icon light" onClick={() => setVisible((value) => !value)} aria-label={visible ? 'Hide balance' : 'Show balance'}><Icon name={visible ? 'eyeOff' : 'eye'} /></button></div>
      <h2>{visible ? money(account.balance, account.currency) : '••••••••'}</h2>
      <div className="account-number-row"><span>{maskAccount(account.accountNumber)}</span><button onClick={() => onCopy(account.accountNumber)} aria-label="Copy account number"><Icon name="copy" size={15}/></button></div>
      {!compact && <div className="balance-actions"><button onClick={() => onNavigate('transfer')}><span><Icon name="arrowUpRight" /></span>Send money</button><button onClick={() => onNavigate('payments')}><span><Icon name="card" /></span>Pay a bill</button><button onClick={() => onNavigate('statements')}><span><Icon name="document" /></span>Statement</button></div>}
      <div className="balance-foot"><span>SecureBank</span><b>{account.currency}</b></div>
    </section>
  );
}
