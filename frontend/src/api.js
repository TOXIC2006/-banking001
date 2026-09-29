export class ApiError extends Error {
  constructor(message, status, code, fieldErrors = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.fieldErrors = fieldErrors;
  }
}

export function createApi(getToken, onUnauthorized) {
  return async function api(path, options = {}) {
    const token = getToken();
    const headers = new Headers(options.headers || {});
    if (token) headers.set('Authorization', `Bearer ${token}`);
    if (options.body && !(options.body instanceof FormData) && typeof options.body !== 'string') {
      headers.set('Content-Type', 'application/json');
    }

    const response = await fetch(path, {
      ...options,
      headers,
      body: options.body && !(options.body instanceof FormData) && typeof options.body !== 'string'
        ? JSON.stringify(options.body)
        : options.body,
    });

    if (response.status === 401 && token) onUnauthorized?.();
    if (!response.ok) {
      let payload = {};
      try { payload = await response.json(); } catch { /* non-JSON response */ }
      const message = payload.message
        || (response.status === 429 ? 'Too many requests. Please wait a moment.' : 'Something went wrong. Please try again.');
      throw new ApiError(message, response.status, payload.code || 'REQUEST_FAILED', payload.fieldErrors);
    }

    if (options.responseType === 'blob') return response.blob();
    if (response.status === 204) return null;
    const type = response.headers.get('content-type') || '';
    return type.includes('application/json') ? response.json() : response.text();
  };
}

export function idempotencyKey(prefix = 'web') {
  const id = globalThis.crypto?.randomUUID?.()
    || `${Date.now()}-${Math.random().toString(16).slice(2)}`;
  return `${prefix}-${id}`;
}
