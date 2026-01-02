/**
 * Date formatting utilities for Danish locale
 */

/**
 * Format ISO 8601 date string or timestamp to Danish locale
 * Handles: ISO 8601 strings (2025-01-02T14:30:00+01:00), Unix timestamps, Date objects
 * @param {string|number|Date} dateInput - Date to format
 * @returns {string} Formatted date in Danish
 */
export const formatDate = (dateInput) => {
  if (!dateInput) return 'N/A';
  
  try {
    let date;
    
    // Handle different input types
    if (dateInput instanceof Date) {
      date = dateInput;
    } else if (typeof dateInput === 'string') {
      // Parse ISO 8601 string (handles OffsetDateTime from backend)
      date = new Date(dateInput);
    } else if (typeof dateInput === 'number') {
      // Handle Unix timestamp (seconds or milliseconds)
      // If number is less than 10000000000, it's in seconds (needs * 1000)
      date = new Date(dateInput < 10000000000 ? dateInput * 1000 : dateInput);
    } else {
      return 'Invalid date';
    }
    
    // Check if date is valid
    if (isNaN(date.getTime())) {
      console.error('Invalid date:', dateInput);
      return 'Invalid date';
    }
    
    // Format to Danish locale
    return date.toLocaleDateString('da-DK', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  } catch (error) {
    console.error('Error formatting date:', dateInput, error);
    return 'Error';
  }
};

/**
 * Format date with time
 * @param {string|number|Date} dateInput - Date to format
 * @returns {string} Formatted date and time in Danish
 */
export const formatDateTime = (dateInput) => {
  if (!dateInput) return 'N/A';
  
  try {
    let date;
    
    if (dateInput instanceof Date) {
      date = dateInput;
    } else if (typeof dateInput === 'string') {
      date = new Date(dateInput);
    } else if (typeof dateInput === 'number') {
      date = new Date(dateInput < 10000000000 ? dateInput * 1000 : dateInput);
    } else {
      return 'Invalid date';
    }
    
    if (isNaN(date.getTime())) {
      console.error('Invalid date:', dateInput);
      return 'Invalid date';
    }
    
    return date.toLocaleString('da-DK', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (error) {
    console.error('Error formatting datetime:', dateInput, error);
    return 'Error';
  }
};

/**
 * Format date to short format (DD/MM/YYYY)
 * @param {string|number|Date} dateInput - Date to format
 * @returns {string} Formatted short date
 */
export const formatDateShort = (dateInput) => {
  if (!dateInput) return 'N/A';
  
  try {
    let date;
    
    if (dateInput instanceof Date) {
      date = dateInput;
    } else if (typeof dateInput === 'string') {
      date = new Date(dateInput);
    } else if (typeof dateInput === 'number') {
      date = new Date(dateInput < 10000000000 ? dateInput * 1000 : dateInput);
    } else {
      return 'Invalid date';
    }
    
    if (isNaN(date.getTime())) {
      return 'Invalid date';
    }
    
    return date.toLocaleDateString('da-DK', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  } catch (error) {
    console.error('Error formatting date short:', dateInput, error);
    return 'Error';
  }
};

/**
 * Get relative time (e.g., "2 days ago", "in 3 months")
 * @param {string|number|Date} dateInput - Date to compare
 * @returns {string} Relative time in Danish
 */
export const formatRelativeTime = (dateInput) => {
  if (!dateInput) return 'N/A';
  
  try {
    let date;
    
    if (dateInput instanceof Date) {
      date = dateInput;
    } else if (typeof dateInput === 'string') {
      date = new Date(dateInput);
    } else if (typeof dateInput === 'number') {
      date = new Date(dateInput < 10000000000 ? dateInput * 1000 : dateInput);
    } else {
      return 'Invalid date';
    }
    
    if (isNaN(date.getTime())) {
      return 'Invalid date';
    }
    
    const now = new Date();
    const diffMs = date - now;
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
    const diffMonths = Math.floor(diffDays / 30);
    
    if (diffDays === 0) return 'I dag';
    if (diffDays === 1) return 'I morgen';
    if (diffDays === -1) return 'I går';
    if (diffDays > 1 && diffDays < 30) return `Om ${diffDays} dage`;
    if (diffDays < -1 && diffDays > -30) return `${Math.abs(diffDays)} dage siden`;
    if (diffMonths > 0) return `Om ${diffMonths} ${diffMonths === 1 ? 'måned' : 'måneder'}`;
    if (diffMonths < 0) return `${Math.abs(diffMonths)} ${Math.abs(diffMonths) === 1 ? 'måned' : 'måneder'} siden`;
    
    return formatDate(date);
  } catch (error) {
    console.error('Error formatting relative time:', dateInput, error);
    return 'Error';
  }
};

