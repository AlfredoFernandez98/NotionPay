import { getItem, setItem, removeItem } from '../utils/storage';

const TOKEN_KEY = 'notionpay_token';
const USER_KEY = 'notionpay_user';

let state = {
  user: getItem(USER_KEY),
  token: getItem(TOKEN_KEY),
  isAuthenticated: !!getItem(TOKEN_KEY),
};

const listeners = new Set();

export const getAuthState = () => state;

export const subscribe = (listener) => {
  listeners.add(listener);
  return () => listeners.delete(listener);
};

const notifyListeners = () => {
  listeners.forEach((listener) => listener(state));
};

export const setAuth = (user, token) => {
  state = {
    user,
    token,
    isAuthenticated: true,
  };
  setItem(USER_KEY, user);
  setItem(TOKEN_KEY, token);
  notifyListeners();
};

export const clearAuth = () => {
  state = {
    user: null,
    token: null,
    isAuthenticated: false,
  };
  removeItem(USER_KEY);
  removeItem(TOKEN_KEY);
  notifyListeners();
};
