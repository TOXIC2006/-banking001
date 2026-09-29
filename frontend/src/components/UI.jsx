import React from 'react';
import Icon from './Icon';

export function PageLoader({ label = 'Loading your banking data' }) {
  return <div className="page-loader"><span className="loader-ring" /><p>{label}</p></div>;
}

export function EmptyState({ icon = 'receipt', title, description, action }) {
  return (
    <div className="empty-state">
      <span className="empty-icon"><Icon name={icon} size={28} /></span>
      <h3>{title}</h3><p>{description}</p>{action}
    </div>
  );
}

export function StatusBadge({ status }) {
  const normalized = (status || 'UNKNOWN').toLowerCase().replaceAll('_', '-');
  return <span className={`status-badge status-${normalized}`}><i />{(status || 'Unknown').replaceAll('_', ' ')}</span>;
}

export function Modal({ title, description, children, onClose, size = 'medium' }) {
  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <section className={`modal modal-${size}`} role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <div className="modal-head"><div><h2 id="modal-title">{title}</h2>{description && <p>{description}</p>}</div><button className="icon-button" onClick={onClose} aria-label="Close"><Icon name="close" /></button></div>
        {children}
      </section>
    </div>
  );
}

export function FieldError({ error }) {
  return error ? <small className="field-error"><Icon name="alert" size={13} />{error}</small> : null;
}

export function SpinnerButton({ loading, children, ...props }) {
  return <button {...props} disabled={loading || props.disabled}>{loading ? <><span className="button-spinner" />Please wait</> : children}</button>;
}
