/**
 * Frontend validation utilities
 */

export const validateEmail = (email) => {
  if (!email) {
    return 'Email is required';
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    return 'Please enter a valid email address';
  }
  return '';
};

export const validatePassword = (password) => {
  if (!password) {
    return 'Password is required';
  }
  if (password.length < 8) {
    return 'Password must be at least 8 characters';
  }
  if (!/\d/.test(password)) {
    return 'Password must contain at least one number';
  }
  return '';
};

export const validateConfirmPassword = (password, confirmPassword) => {
  if (!confirmPassword) {
    return 'Please confirm your password';
  }
  if (password !== confirmPassword) {
    return 'Passwords do not match';
  }
  return '';
};

export const validateCompanyName = (companyName) => {
  if (!companyName) {
    return 'Company name is required';
  }
  if (companyName.trim().length === 0) {
    return 'Company name cannot be empty';
  }
  if (companyName.length > 100) {
    return 'Company name must be less than 100 characters';
  }
  return '';
};

export const validateSerialNumber = (serialNumber) => {
  if (!serialNumber) {
    return 'Serial number is required';
  }
  if (!/^\d+$/.test(serialNumber)) {
    return 'Serial number must contain only digits';
  }
  return '';
};

export const validateCardNumber = (cardNumber) => {
  if (!cardNumber) {
    return 'Card number is required';
  }
  const cleaned = cardNumber.replace(/\s/g, '');
  if (!/^\d{13,19}$/.test(cleaned)) {
    return 'Please enter a valid card number';
  }
  return '';
};

export const validateCVC = (cvc) => {
  if (!cvc) {
    return 'CVC is required';
  }
  if (!/^\d{3,4}$/.test(cvc)) {
    return 'CVC must be 3 or 4 digits';
  }
  return '';
};

export const validateExpiryMonth = (month) => {
  if (!month) {
    return 'Expiry month is required';
  }
  const monthNum = parseInt(month);
  if (isNaN(monthNum) || monthNum < 1 || monthNum > 12) {
    return 'Month must be between 1 and 12';
  }
  return '';
};

export const validateExpiryYear = (year) => {
  if (!year) {
    return 'Expiry year is required';
  }
  const yearNum = parseInt(year);
  const currentYear = new Date().getFullYear();
  if (isNaN(yearNum) || yearNum < currentYear) {
    return 'Card has expired';
  }
  if (yearNum > currentYear + 20) {
    return 'Invalid expiry year';
  }
  return '';
};

