import React, { Component, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { createApi } from './api';
import AuthScreen from './components/AuthScreen';
import AppShell from './components/AppShell';
import Icon from './components/Icon';
import Toast from './components/Toast';
import Dashboard from './pages/Dashboard';
import Transfer from './pages/Transfer';
import Payments from './pages/Payments';
import Statements from './pages/Statements';
import Notifications from './pages/Notifications';
import Support from './pages/Support';
import Profile from './pages/Profile';

const routes = new Set(['dashboard', 'transfer', 'payments', 'statements', 'notifications', 'support', 'profile']);
function routeFromHash() { const value = window.location.hash.replace('#/', '').replace('#', ''); return routes.has(value) ? value : 'dashboard'; }

class ErrorBoundary extends Component {
  state = { failed: false };
  static getDerivedStateFromError() { return { failed: true }; }
  render() {
    if (this.state.failed) return <div className="fatal-state"><span><Icon name="alert" size={32}/></span><h2>This page needs a quick refresh</h2><p>Your banking data is safe. Reload to restore the interface.</p><button className="primary-button" onClick={() => window.location.reload()}><Icon name="refresh"/>Reload SecureBank</button></div>;
    return this.props.children;
  }
}

export default function App() {
  const [token, setToken] = useState(() => sessionStorage.getItem('securebank_token') || localStorage.getItem('securebank_token'));
  const [account, setAccount] = useState(null);
  const [checking, setChecking] = useState(Boolean(token));
  const [route, setRoute] = useState(routeFromHash);
  const [toast, setToast] = useState(null);
  const [notificationCount, setNotificationCount] = useState(0);
  const tokenRef = useRef(token);
  tokenRef.current = token;

  const logout = useCallback((expired = false) => {
    sessionStorage.removeItem('securebank_token'); localStorage.removeItem('securebank_token');
    tokenRef.current = null; setToken(null); setAccount(null); setNotificationCount(0);
    window.location.hash = '';
    if (expired === true) setToast({ id: Date.now(), title: 'Session ended', message: 'Please sign in again to continue.', type: 'error' });
  }, []);

  const api = useMemo(() => createApi(() => tokenRef.current, () => logout(true)), [logout]);
  const notify = useCallback((title, message, type = 'success') => setToast({ id: Date.now(), title, message, type }), []);

  const refreshAccount = useCallback(async () => {
    const profile = await api('/api/accounts/me'); setAccount(profile); return profile;
  }, [api]);

  useEffect(() => {
    const handler = () => setRoute(routeFromHash());
    window.addEventListener('hashchange', handler);
    return () => window.removeEventListener('hashchange', handler);
  }, []);

  useEffect(() => {
    if (!token) { setChecking(false); return; }
    setChecking(true);
    Promise.allSettled([refreshAccount(), api('/api/notifications')])
      .then(([profile, notes]) => {
        if (profile.status === 'rejected' && profile.reason?.status !== 401) notify('Unable to load account', profile.reason?.message || 'Try again in a moment.', 'error');
        if (notes.status === 'fulfilled') setNotificationCount(notes.value.filter((item) => item.status !== 'SENT').length);
      }).finally(() => setChecking(false));
  }, [token, api, refreshAccount, notify]);

  const authenticated = (nextToken, remember) => {
    sessionStorage.removeItem('securebank_token'); localStorage.removeItem('securebank_token');
    (remember ? localStorage : sessionStorage).setItem('securebank_token', nextToken);
    tokenRef.current = nextToken; setToken(nextToken); setChecking(true);
  };

  const navigate = (next) => {
    if (!routes.has(next)) return;
    window.location.hash = `#/${next}`; setRoute(next); window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  let page = null;
  if (account) {
    const common = { account, api, navigate, notify, refreshAccount };
    if (route === 'transfer') page = <Transfer {...common}/>;
    else if (route === 'payments') page = <Payments {...common}/>;
    else if (route === 'statements') page = <Statements {...common}/>;
    else if (route === 'notifications') page = <Notifications {...common}/>;
    else if (route === 'support') page = <Support {...common}/>;
    else if (route === 'profile') page = <Profile {...common} onLogout={() => logout(false)}/>;
    else page = <Dashboard {...common}/>;
  }

  return (
    <ErrorBoundary>
      {checking && <div className="app-boot"><div className="boot-shield"><Icon name="shield" size={34}/></div><span className="loader-ring"/><p>Opening your secure account…</p></div>}
      {!checking && !account && <AuthScreen api={api} onAuthenticated={authenticated} notify={notify}/>}
      {!checking && account && <AppShell account={account} route={route} navigate={navigate} notificationCount={notificationCount} onLogout={() => logout(false)}>{page}</AppShell>}
      <div className="toast-region">{toast && <Toast key={toast.id} toast={toast} onClose={() => setToast(null)}/>}</div>
    </ErrorBoundary>
  );
}
