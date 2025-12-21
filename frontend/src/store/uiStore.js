let state = {
  isLoading: false,
  error: null,
  notification: null,
};

const listeners = new Set();

export const getUIState = () => state;

export const subscribe = (listener) => {
  listeners.add(listener);
  return () => listeners.delete(listener);
};

const notifyListeners = () => {
  listeners.forEach((listener) => listener(state));
};

export const setLoading = (isLoading) => {
  state = { ...state, isLoading };
  notifyListeners();
};

export const setError = (error) => {
  state = { ...state, error };
  notifyListeners();
};

export const setNotification = (notification) => {
  state = { ...state, notification };
  notifyListeners();
};

export const clearNotification = () => {
  state = { ...state, notification: null };
  notifyListeners();
};
