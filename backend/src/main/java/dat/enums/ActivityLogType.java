package dat.enums;

public enum ActivityLogType {
    // Authentication
    LOGIN,
    LOGOUT,
    
    // Payment & Cards
    PAYMENT,
    ADD_CARD,
    REMOVE_CARD,
    
    // Subscription
    SUBSCRIPTION_CREATED,
    SUBSCRIPTION_CANCELLED,
    SUBSCRIPTION_RENEWED,
    
    // Profile
    PASSWORD_CHANGED,
    PROFILE_UPDATED,
    
    // SMS
    SMS_SENT,
    SMS_PURCHASE
}

