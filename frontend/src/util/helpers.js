/**
 * Helper utility functions
 */

/**
 * Format currency in cents to display format
 */
export const formatCurrency = (cents, currency = 'USD') => {
  const amount = cents / 100
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency,
  }).format(amount)
}

/**
 * Format date to readable string
 */
export const formatDate = (dateString) => {
  const date = new Date(dateString)
  return new Intl.DateFormat('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(date)
}

/**
 * Format date and time
 */
export const formatDateTime = (dateString) => {
  const date = new Date(dateString)
  return new Intl.DateFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

/**
 * Get local storage item safely
 */
export const getStorageItem = (key, defaultValue = null) => {
  try {
    const item = localStorage.getItem(key)
    return item ? JSON.parse(item) : defaultValue
  } catch (error) {
    console.error(`Error reading from localStorage:`, error)
    return defaultValue
  }
}

/**
 * Set local storage item safely
 */
export const setStorageItem = (key, value) => {
  try {
    localStorage.setItem(key, JSON.stringify(value))
    return true
  } catch (error) {
    console.error(`Error writing to localStorage:`, error)
    return false
  }
}

/**
 * Remove local storage item
 */
export const removeStorageItem = (key) => {
  try {
    localStorage.removeItem(key)
    return true
  } catch (error) {
    console.error(`Error removing from localStorage:`, error)
    return false
  }
}

/**
 * Validate email format
 */
export const isValidEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

/**
 * Mask credit card number
 */
export const maskCardNumber = (cardNumber) => {
  return `**** **** **** ${cardNumber.slice(-4)}`
}
