import React, { useState } from 'react';
import Icon from './Icon';
import Logo from './Logo';
import { SpinnerButton } from './UI';

const initialRegister = { fullName: '', email: '', phone: '', currency: 'INR', password: '' };

export default function AuthScreen({ api, onAuthenticated, notify }) {
  const [mode, setMode] = useState('login');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [login, setLogin] = useState({ email: '', password: '', remember: false });
  const [register, setRegister] = useState(initialRegister);
  const [terms, setTerms] = useState(false);

  const switchMode = (next) => {
    setMode(next); setError(''); setShowPassword(false);
  };

  const submitLogin = async (event, override = null) => {
    event?.preventDefault();
    setError(''); setLoading(true);
    const credentials = override || login;
    try {
      const token = await api('/api/auth/login', {
        method: 'POST', body: { email: credentials.email.trim(), password: credentials.password },
      });
      onAuthenticated(token.accessToken, credentials.remember);
    } catch (requestError) {
      setError(requestError.message || 'Unable to sign in right now.');
    } finally { setLoading(false); }
  };

  const submitRegister = async (event) => {
    event.preventDefault(); setError('');
    if (!terms) { setError('Please accept the terms and privacy policy.'); return; }
    setLoading(true);
    try {
      await api('/api/accounts/onboard', {
        method: 'POST',
        body: { ...register, fullName: register.fullName.trim(), email: register.email.trim(), phone: register.phone.replaceAll(' ', '') },
      });
      notify('Account created', 'Welcome to SecureBank. Signing you in now.');
      await submitLogin(null, { email: register.email, password: register.password, remember: true });
    } catch (requestError) {
      setError(requestError.message || 'We could not open your account.');
      setLoading(false);
    }
  };

  return (
    <main className="auth-view">
      <section className="auth-visual" aria-label="SecureBank introduction">
        <Logo light />
        <div className="auth-copy">
          <p className="eyebrow light"><i /> Digital banking, simplified</p>
          <h1>Money moves.<br/><em>Confidence stays.</em></h1>
          <p>One secure place to manage your money, make instant payments, and stay in control—every day.</p>
        </div>
        <div className="card-stage" aria-hidden="true">
          <div className="orbit orbit-one"/><div className="orbit orbit-two"/>
          <div className="bank-card bank-card-back"/>
          <div className="bank-card bank-card-front">
            <div className="card-shine"/>
            <div className="card-top"><span><b>S</b> SecureBank</span><svg viewBox="0 0 24 24"><path d="M8.5 8.5a5 5 0 0 1 0 7M12 6a8.5 8.5 0 0 1 0 12M15.5 3.5a12 12 0 0 1 0 17" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/></svg></div>
            <div className="chip"><i/><i/><i/><i/></div>
            <p className="card-number">•••• &nbsp;•••• &nbsp;•••• &nbsp;4821</p>
            <div className="card-bottom"><span><small>CARD HOLDER</small>YOUR NAME</span><span><small>VALID THRU</small>08/30</span><strong>VISA</strong></div>
          </div>
          <div className="balance-float"><span className="float-icon"><Icon name="arrowUpRight" size={17}/></span><span><small>Transfer complete</small><b>₹12,400.00</b></span><i><Icon name="check" size={14}/></i></div>
        </div>
        <div className="auth-proof"><span><i><Icon name="shield"/></i>Bank-grade security</span><span><i><Icon name="lightning"/></i>Real-time payments</span><span><i><Icon name="support"/></i>Always-on support</span></div>
        <p className="visual-foot">Protected by encryption and continuous fraud monitoring.</p>
      </section>

      <section className="auth-panel">
        <div className="mobile-auth-brand"><Logo /></div>
        <div className="auth-form-wrap">
          <header className="auth-heading">
            <p className="eyebrow"><i />{mode === 'login' ? 'Welcome back' : 'Join SecureBank'}</p>
            <h2>{mode === 'login' ? 'Sign in to your account' : 'Open your bank account'}</h2>
            <p>{mode === 'login' ? 'Your finances, protected and within reach.' : 'Start secure digital banking in a few minutes.'}</p>
          </header>
          <div className="auth-tabs" role="tablist">
            <button className={mode === 'login' ? 'active' : ''} type="button" onClick={() => switchMode('login')} role="tab" aria-selected={mode === 'login'}>Sign in</button>
            <button className={mode === 'register' ? 'active' : ''} type="button" onClick={() => switchMode('register')} role="tab" aria-selected={mode === 'register'}>Open an account</button>
          </div>

          {error && <div className="form-alert"><Icon name="alert" size={18}/><span>{error}</span></div>}

          {mode === 'login' ? (
            <form className="auth-form" onSubmit={submitLogin}>
              <label htmlFor="login-email">Email address</label>
              <div className="input-shell"><Icon name="mail"/><input id="login-email" type="email" autoComplete="email" placeholder="you@example.com" value={login.email} onChange={(e) => setLogin({...login, email: e.target.value})} required/></div>
              <div className="label-row"><label htmlFor="login-password">Password</label><button type="button" className="text-button" onClick={() => notify('Password help', 'Contact support to securely restore access to your account.')}>Need help?</button></div>
              <div className="input-shell"><Icon name="lock"/><input id="login-password" type={showPassword ? 'text' : 'password'} autoComplete="current-password" placeholder="Enter your password" value={login.password} onChange={(e) => setLogin({...login, password: e.target.value})} required/><button className="password-toggle" type="button" onClick={() => setShowPassword(!showPassword)} aria-label="Toggle password visibility"><Icon name={showPassword ? 'eyeOff' : 'eye'}/></button></div>
              <label className="check-row"><input type="checkbox" checked={login.remember} onChange={(e) => setLogin({...login, remember: e.target.checked})}/><span className="custom-check"><Icon name="check" size={13}/></span><span>Keep me signed in on this device</span></label>
              <SpinnerButton loading={loading} className="primary-button auth-submit" type="submit"><span>Sign in securely</span><Icon name="arrowRight"/></SpinnerButton>
              <div className="secure-note"><Icon name="shield" size={16}/>Secured with JWT authentication and fraud monitoring</div>
            </form>
          ) : (
            <form className="auth-form register-form" onSubmit={submitRegister}>
              <label htmlFor="register-name">Full name</label>
              <div className="input-shell"><Icon name="user"/><input id="register-name" autoComplete="name" placeholder="Your full name" value={register.fullName} onChange={(e) => setRegister({...register, fullName: e.target.value})} maxLength={160} required/></div>
              <label htmlFor="register-email">Email address</label>
              <div className="input-shell"><Icon name="mail"/><input id="register-email" type="email" autoComplete="email" placeholder="you@example.com" value={register.email} onChange={(e) => setRegister({...register, email: e.target.value})} required/></div>
              <div className="split-fields"><div><label htmlFor="register-phone">Phone</label><div className="input-shell"><Icon name="phone"/><input id="register-phone" type="tel" autoComplete="tel" placeholder="+91 98765 43210" value={register.phone} onChange={(e) => setRegister({...register, phone: e.target.value})} required/></div></div><div><label htmlFor="register-currency">Currency</label><div className="input-shell"><Icon name="wallet"/><select id="register-currency" value={register.currency} onChange={(e) => setRegister({...register, currency: e.target.value})}><option value="INR">INR — Rupee</option><option value="USD">USD — Dollar</option><option value="EUR">EUR — Euro</option><option value="GBP">GBP — Pound</option></select></div></div></div>
              <label htmlFor="register-password">Create password</label>
              <div className="input-shell"><Icon name="lock"/><input id="register-password" type={showPassword ? 'text' : 'password'} autoComplete="new-password" placeholder="At least 10 characters" minLength={10} maxLength={72} value={register.password} onChange={(e) => setRegister({...register, password: e.target.value})} required/><button className="password-toggle" type="button" onClick={() => setShowPassword(!showPassword)}><Icon name={showPassword ? 'eyeOff' : 'eye'}/></button></div>
              <label className="check-row"><input type="checkbox" checked={terms} onChange={(e) => setTerms(e.target.checked)}/><span className="custom-check"><Icon name="check" size={13}/></span><span>I agree to the terms and privacy policy.</span></label>
              <SpinnerButton loading={loading} className="primary-button auth-submit" type="submit"><span>Open my account</span><Icon name="arrowRight"/></SpinnerButton>
            </form>
          )}
        </div>
        <p className="auth-legal">© 2026 SecureBank. Built for safer banking.</p>
      </section>
    </main>
  );
}
