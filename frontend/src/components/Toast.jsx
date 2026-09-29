import React, { useEffect } from 'react';
import Icon from './Icon';

export default function Toast({ toast, onClose }) {
  useEffect(() => {
    const timer = setTimeout(onClose, toast.duration || 4500);
    return () => clearTimeout(timer);
  }, [toast, onClose]);

  return (
    <div className={`toast toast-${toast.type || 'success'}`} role="status">
      <span className="toast-icon"><Icon name={toast.type === 'error' ? 'alert' : 'check'} size={18} /></span>
      <div><strong>{toast.title || (toast.type === 'error' ? 'Action failed' : 'Done')}</strong><p>{toast.message}</p></div>
      <button onClick={onClose} aria-label="Close notification"><Icon name="close" size={17} /></button>
    </div>
  );
}
