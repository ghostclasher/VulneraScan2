const TOKEN_KEY = 'vulnera_token';

export function saveToken(t) { localStorage.setItem(TOKEN_KEY, t); }
export function getToken() { return localStorage.getItem(TOKEN_KEY); }
export function removeToken() { localStorage.removeItem(TOKEN_KEY); }