import React, { useEffect, useState } from 'react';
import Icon from './Icon';
import Logo from './Logo';
import { firstName, initials } from '../utils';

const navGroups = [
  { label: 'Overview', items: [
    { route: 'dashboard', label: 'Dashboard', icon: 'grid' },
    { route: 'transfer', label: 'Send money', icon: 'transfer' },
    { route: 'payments', label: 'Payments', icon: 'card' },
    { route: 'statements', label: 'Statements', icon: 'document' },
  ] },
  { label: 'Manage', items: [
    { route: 'notifications', label: 'Notifications', icon: 'bell' },
    { route: 'support', label: 'Help & support', icon: 'help' },
    { route: 'profile', label: 'Profile & KYC', icon: 'user' },
  ] },
];

const pageMeta = {
  dashboard: ['Overview', 'Dashboard'], transfer: ['Payments', 'Send money'], payments: ['Money management', 'Payments'],
  statements: ['Account', 'Statements'], notifications: ['Updates', 'Notifications'], support: ['Care', 'Help & support'], profile: ['Account', 'Profile & KYC'],
};

export default function AppShell({ account, route, navigate, notificationCount, onLogout, children }) {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  useEffect(() => setSidebarOpen(false), [route]);
  const select = (next) => { navigate(next); setSidebarOpen(false); };
  const meta = pageMeta[route] || pageMeta.dashboard;

  return (
    <div className="app-view">
      <button className={`sidebar-overlay ${sidebarOpen ? 'visible' : ''}`} aria-label="Close navigation" onClick={() => setSidebarOpen(false)}/>
      <aside className={`sidebar ${sidebarOpen ? 'open' : ''}`}>
        <div className="sidebar-head"><button className="logo-button" onClick={() => select('dashboard')}><Logo light/></button><button className="icon-button sidebar-close" onClick={() => setSidebarOpen(false)} aria-label="Close menu"><Icon name="close"/></button></div>
        <nav className="sidebar-nav">
          {navGroups.map((group) => <div className="nav-group" key={group.label}><p>{group.label}</p>{group.items.map((item) => <button key={item.route} className={`nav-item ${route === item.route ? 'active' : ''}`} onClick={() => select(item.route)}><Icon name={item.icon}/><span>{item.label}</span>{item.route === 'notifications' && notificationCount > 0 && <i className="nav-count">{notificationCount > 9 ? '9+' : notificationCount}</i>}</button>)}</div>)}
        </nav>
        <div className="sidebar-security"><span><Icon name="shield"/></span><div><strong>Your account is protected</strong><small>Fraud monitoring is active.</small></div></div>
        <div className="sidebar-user"><span className="avatar">{initials(account.fullName)}</span><div><strong>{account.fullName}</strong><small>{account.email}</small></div><button className="icon-button inverse" onClick={onLogout} aria-label="Sign out"><Icon name="logout"/></button></div>
      </aside>

      <div className="app-body">
        <header className="topbar">
          <div className="topbar-left"><button className="icon-button menu-button" onClick={() => setSidebarOpen(true)} aria-label="Open menu"><Icon name="menu"/></button><div><p>{meta[0]}</p><h1>{meta[1]}</h1></div></div>
          <div className="topbar-right"><span className="security-pill"><i/>Secure session</span><button className="icon-button topbar-action" onClick={() => select('notifications')} aria-label="Notifications"><Icon name="bell"/>{notificationCount > 0 && <i className="notification-badge">{notificationCount > 9 ? '9+' : notificationCount}</i>}</button><button className="profile-chip" onClick={() => select('profile')}><span className="avatar small">{initials(account.fullName)}</span><b>{firstName(account.fullName)}</b><Icon name="chevron" size={16}/></button></div>
        </header>
        <main className="page-root">{children}</main>
        <footer className="app-footer"><span>SecureBank • Your money, protected</span><span>Encrypted <i/> Fraud monitoring active</span></footer>
      </div>

      <nav className="mobile-nav">
        <button className={route === 'dashboard' ? 'active' : ''} onClick={() => select('dashboard')}><Icon name="grid"/><small>Home</small></button>
        <button className={route === 'transfer' ? 'active' : ''} onClick={() => select('transfer')}><Icon name="transfer"/><small>Send</small></button>
        <button className="mobile-main-action" onClick={() => select('payments')}><Icon name="plus"/></button>
        <button className={route === 'statements' ? 'active' : ''} onClick={() => select('statements')}><Icon name="document"/><small>Activity</small></button>
        <button className={route === 'profile' ? 'active' : ''} onClick={() => select('profile')}><Icon name="user"/><small>Profile</small></button>
      </nav>
    </div>
  );
}
