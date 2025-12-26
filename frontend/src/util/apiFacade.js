/**
 * API Facade for NotionPay Frontend
 * Handles all backend communication with authentication, token management, and error handling
 */

const URL = import.meta.env.VITE_API_URL || "http://localhost:7070/api";

/**
 * Handle HTTP errors from backend responses
 */
function handleHttpErrors(res) {
  if (!res.ok) {
    return Promise.reject({ 
      status: res.status, 
      fullError: res.json() 
    });
  }
  return res.json();
}

function apiFacade() {
  /**
   * TOKEN MANAGEMENT
   */
  const setToken = (token) => {
    localStorage.setItem("jwtToken", token);
  };

  const getToken = () => {
    return localStorage.getItem("jwtToken");
  };

  const loggedIn = () => {
    return getToken() != null;
  };

  const removeToken = () => {
    localStorage.removeItem("jwtToken");
  };

  /**
   * SESSION MANAGEMENT
   */
  const setSessionId = (sessionId) => {
    localStorage.setItem("sessionId", sessionId);
  };

  const getSessionId = () => {
    return localStorage.getItem("sessionId");
  };

  const removeSessionId = () => {
    localStorage.removeItem("sessionId");
  };

  /**
   * USER DATA MANAGEMENT
   */
  const setUserEmail = (email) => {
    localStorage.setItem("userEmail", email);
  };

  const getUserEmail = () => {
    return localStorage.getItem("userEmail");
  };

  const removeUserEmail = () => {
    localStorage.removeItem("userEmail");
  };

  const setCustomerId = (customerId) => {
    localStorage.setItem("customerId", customerId);
  };

  const getCustomerId = () => {
    return localStorage.getItem("customerId");
  };

  const removeCustomerId = () => {
    localStorage.removeItem("customerId");
  };

  /**
   * CREATE REQUEST OPTIONS
   */
  const makeOptions = (method, addToken, body) => {
    const opts = {
      method: method,
      headers: {
        "Content-Type": "application/json",
        "Accept": "application/json",
      },
    };

    if (addToken && loggedIn()) {
      opts.headers["Authorization"] = `Bearer ${getToken()}`;
    }

    if (body) {
      opts.body = JSON.stringify(body);
    }

    return opts;
  };

  /**
   * LOGIN
   * Backend endpoint: POST /api/auth/login
   * Request: { email, password }
   * Response: { token, email, sessionID }
   */
  const login = (email, password) => {
    const options = makeOptions("POST", false, {
      email: email,
      password: password,
    });

    console.log("🔐 LOGIN REQUEST:");
    console.log("  URL:", URL + "/auth/login");
    console.log("  Email:", email);
    console.log("  Password length:", password?.length);
    console.log("  Password (DEBUG):", password);
    console.log("  Request body:", JSON.stringify({ email, password }));
    console.log("  Options:", options);

    return fetch(URL + "/auth/login", options)
      .then((response) => {
        console.log("📡 Backend response status:", response.status);
        console.log("📡 Response OK:", response.ok);
        return handleHttpErrors(response);
      })
      .then((res) => {
        console.log("✅ Login response data:", res);
        
        // Store authentication data
        setToken(res.token);
        setUserEmail(res.email);
        setSessionId(res.sessionID);
        
        console.log("✅ Login successful:", res.email);
        return res;
      })
      .catch((error) => {
        console.error("❌ Login failed - Full error:", error);
        console.error("❌ Error status:", error.status);
        console.error("❌ Error fullError:", error.fullError);
        throw error;
      });
  };

  /**
   * REGISTER
   * Backend endpoint: POST /api/auth/register
   * Request: { email, password, companyName, serialNumber }
   * Response: { token, email, customerId, subscriptionId, planId, planName, initialSmsCredits, msg }
   */
  const register = (email, password, companyName, serialNumber) => {
    const options = makeOptions("POST", false, {
      email: email,
      password: password,
      companyName: companyName,
      serialNumber: serialNumber,
    });

    console.log("📝 REGISTER REQUEST:");
    console.log("  URL:", URL + "/auth/register");
    console.log("  Email:", email);
    console.log("  Password length:", password?.length);
    console.log("  Password (DEBUG):", password);
    console.log("  Request body:", JSON.stringify({ email, password, companyName, serialNumber }));

    return fetch(URL + "/auth/register", options)
      .then(handleHttpErrors)
      .then((res) => {
        // Store authentication data
        setToken(res.token);
        setUserEmail(res.email);
        setCustomerId(res.customerId);
        
        console.log("Registration successful:", res.email);
        console.log("Subscribed to plan:", res.planName);
        console.log("Initial SMS credits:", res.initialSmsCredits);
        
        return res;
      })
      .catch((error) => {
        console.error("Registration failed:", error);
        throw error;
      });
  };

  /**
   * VALIDATE TOKEN
   * Backend endpoint: POST /api/auth/validate
   * Checks if current token is still valid
   * Response: { msg, email, sessionID, expiresAt }
   */
  const validateToken = () => {
    if (!loggedIn()) {
      return Promise.reject({ msg: "No token found" });
    }

    const options = makeOptions("POST", true);

    return fetch(URL + "/auth/validate", options)
      .then(handleHttpErrors)
      .then((res) => {
        console.log("Token is valid, expires at:", res.expiresAt);
        return res;
      })
      .catch((error) => {
        console.error("Token validation failed:", error);
        // If token is invalid, clear local storage
        logout();
        throw error;
      });
  };

  /**
   * LOGOUT
   * Backend endpoint: POST /api/auth/logout
   * Deactivates the session in the database
   * Response: { msg }
   */
  const logoutWithBackend = () => {
    if (!loggedIn()) {
      console.warn("Cannot logout - no active session");
      return Promise.resolve();
    }

    const options = makeOptions("POST", true);

    return fetch(URL + "/auth/logout", options)
      .then(handleHttpErrors)
      .then((res) => {
        console.log("Logout successful:", res.msg);
        // Clear all stored data
        clearAllData();
        return res;
      })
      .catch((error) => {
        console.error("Logout failed:", error);
        // Clear data anyway even if backend call fails
        clearAllData();
        throw error;
      });
  };

  /**
   * LOCAL LOGOUT (without backend call)
   * Use this if you just want to clear local data
   */
  const logout = () => {
    clearAllData();
    console.log("Logged out locally");
  };

  /**
   * CLEAR ALL STORED DATA
   */
  const clearAllData = () => {
    removeToken();
    removeSessionId();
    removeUserEmail();
    removeCustomerId();
  };

  /**
   * GENERIC FETCH DATA
   * Use this for any authenticated API call
   * @param {string} endpoint - API endpoint (e.g., "/customers/1")
   * @param {string} method - HTTP method (default: "GET")
   * @param {object} body - Request body (optional)
   */
  const fetchData = (endpoint, method = "GET", body = null) => {
    const options = makeOptions(method, true, body);
    return fetch(URL + endpoint, options).then(handleHttpErrors);
  };

  /**
   * JWT TOKEN DECODING
   * Extract information from JWT token
   */
  const getUserRoles = () => {
    const token = getToken();
    if (token != null) {
      try {
        const payloadBase64 = token.split('.')[1];
        const decodedClaims = JSON.parse(window.atob(payloadBase64));
        const roles = decodedClaims.roles;
        return roles || "";
      } catch (error) {
        console.error("Error decoding token:", error);
        return "";
      }
    }
    return "";
  };

  const hasUserAccess = (neededRole, isLoggedIn) => {
    const rolesString = getUserRoles();
    if (!rolesString) return false;
    
    const roles = rolesString.split(',');
    return isLoggedIn && roles.includes(neededRole);
  };

  const getEmailFromToken = () => {
    const token = getToken();
    if (token != null) {
      try {
        const payloadBase64 = token.split('.')[1];
        const decodedClaims = JSON.parse(window.atob(payloadBase64));
        return decodedClaims.email || decodedClaims.sub || "";
      } catch (error) {
        console.error("Error decoding token:", error);
        return "";
      }
    }
    return "";
  };

  const getTokenExpiration = () => {
    const token = getToken();
    if (token != null) {
      try {
        const payloadBase64 = token.split('.')[1];
        const decodedClaims = JSON.parse(window.atob(payloadBase64));
        // exp is in seconds, convert to milliseconds
        return decodedClaims.exp ? new Date(decodedClaims.exp * 1000) : null;
      } catch (error) {
        console.error("Error decoding token:", error);
        return null;
      }
    }
    return null;
  };

  const isTokenExpired = () => {
    const expiration = getTokenExpiration();
    if (!expiration) return true;
    return new Date() > expiration;
  };

  /**
   * CUSTOMER API CALLS
   */
  const getCustomerProfile = (customerId) => {
    return fetchData(`/customers/${customerId}`, "GET");
  };

  const updateCustomerProfile = (customerId, data) => {
    return fetchData(`/customers/${customerId}`, "PUT", data);
  };

  /**
   * SUBSCRIPTION API CALLS
   */
  const getCustomerSubscription = (customerId) => {
    return fetchData(`/customers/${customerId}/subscription`, "GET");
  };

  /**
   * PAYMENT API CALLS
   */
  const getCustomerPaymentMethods = (customerId) => {
    return fetchData(`/customers/${customerId}/payment-methods`, "GET");
  };

  const addPaymentMethod = (data) => {
    return fetchData("/payment-methods", "POST", data);
  };

  const processPayment = (data) => {
    return fetchData("/payments", "POST", data);
  };

  const getPaymentById = (paymentId) => {
    return fetchData(`/payments/${paymentId}`, "GET");
  };

  const getReceiptForPayment = (paymentId) => {
    return fetchData(`/payments/${paymentId}/receipt`, "GET");
  };

  /**
   * PLAN API CALLS
   */
  const getAllPlans = () => {
    return fetchData("/plans", "GET");
  };

  const getPlanById = (planId) => {
    return fetchData(`/plans/${planId}`, "GET");
  };

  /**
   * PRODUCT API CALLS (SMS Products)
   */
  const getAllProducts = () => {
    return fetchData("/products", "GET");
  };

  const getSmsProducts = () => {
    return fetchData("/products/sms", "GET");
  };

  /**
   * SMS BALANCE API CALLS
   */
  const getSmsBalance = (customerId) => {
    return fetchData(`/customers/${customerId}/sms-balance`, "GET");
  };

  /**
   * RECEIPT API CALLS
   */
  const getCustomerReceipts = (customerId) => {
    return fetchData(`/customers/${customerId}/receipts`, "GET");
  };

  const getReceiptById = (receiptId) => {
    return fetchData(`/receipts/${receiptId}`, "GET");
  };

  const getReceiptByNumber = (receiptNumber) => {
    return fetchData(`/receipts/number/${receiptNumber}`, "GET");
  };

  return {
    // Configuration
    makeOptions,
    
    // Token Management
    setToken,
    getToken,
    loggedIn,
    
    // Authentication
    login,
    register,
    validateToken,
    logout,
    logoutWithBackend,
    
    // Session Management
    getSessionId,
    
    // User Data
    getUserEmail,
    getCustomerId,
    
    // Generic Data Fetching
    fetchData,
    
    // JWT Decoding
    getUserRoles,
    hasUserAccess,
    getEmailFromToken,
    getTokenExpiration,
    isTokenExpired,
    
    // Customer API
    getCustomerProfile,
    updateCustomerProfile,
    
    // Subscription API
    getCustomerSubscription,
    
    // Payment API
    getCustomerPaymentMethods,
    addPaymentMethod,
    processPayment,
    getPaymentById,
    getReceiptForPayment,
    
    // Plan API
    getAllPlans,
    getPlanById,
    
    // Product API
    getAllProducts,
    getSmsProducts,
    
    // SMS Balance API
    getSmsBalance,
    
    // Receipt API
    getCustomerReceipts,
    getReceiptById,
    getReceiptByNumber,
  };
}

// Create singleton instance
const facade = apiFacade();
export default facade;
