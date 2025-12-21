import { useState, useEffect } from 'react';
import { getAuthState, subscribe, setAuth, clearAuth } from '../store/authStore';

export const useAuth = () => {
  const [authState, setAuthState] = useState(getAuthState());

  useEffect(() => {
    const unsubscribe = subscribe((newState) => {
      setAuthState(newState);
    });
    return unsubscribe;
  }, []);

  return {
    ...authState,
    login: (user, token) => setAuth(user, token),
    logout: () => clearAuth(),
  };
};
