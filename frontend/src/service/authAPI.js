import { request } from './http';

export const login = async (credentials) => {
  return request('/auth/login', {
    method: 'POST',
    body: JSON.stringify(credentials),
  });
};

export const register = async (registerData) => {
  return request('/auth/register', {
    method: 'POST',
    body: JSON.stringify(registerData),
  });
};

export const me = async () => {
  return request('/auth/me', {
    method: 'GET',
  });
};
