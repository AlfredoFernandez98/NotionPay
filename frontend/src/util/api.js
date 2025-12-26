/**
 * API utility functions for backend communication
 */

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:7070'

/**
 * Generic fetch wrapper with error handling
 */
export const apiFetch = async (endpoint, options = {}) => {
  const url = `${API_BASE_URL}${endpoint}`
  
  const defaultOptions = {
    headers: {
      'Content-Type': 'application/json',
    },
  }
  
  const config = {
    ...defaultOptions,
    ...options,
    headers: {
      ...defaultOptions.headers,
      ...options.headers,
    },
  }
  
  try {
    const response = await fetch(url, config)
    
    if (!response.ok) {
      const error = await response.json().catch(() => ({}))
      throw new Error(error.msg || `HTTP error! status: ${response.status}`)
    }
    
    return await response.json()
  } catch (error) {
    console.error('API Error:', error)
    throw error
  }
}

/**
 * Authentication API calls
 */
export const authAPI = {
  login: (email, password) => 
    apiFetch('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),
  
  register: (email, password, companyName) => 
    apiFetch('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ email, password, companyName }),
    }),
  
  logout: (token) => 
    apiFetch('/api/auth/logout', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    }),
}

/**
 * Customer API calls
 */
export const customerAPI = {
  getProfile: (customerId, token) => 
    apiFetch(`/api/customers/${customerId}`, {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    }),
}

/**
 * Payment API calls
 */
export const paymentAPI = {
  addPaymentMethod: (data, token) => 
    apiFetch('/api/payment-methods', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
      body: JSON.stringify(data),
    }),
  
  processPayment: (data, token) => 
    apiFetch('/api/payments', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
      body: JSON.stringify(data),
    }),
  
  getPayments: (customerId, token) => 
    apiFetch(`/api/customers/${customerId}/payments`, {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    }),
}

/**
 * Subscription API calls
 */
export const subscriptionAPI = {
  getSubscription: (customerId, token) => 
    apiFetch(`/api/customers/${customerId}/subscription`, {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    }),
}
