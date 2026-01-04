package dat.utils;

import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Utility class for date/time operations with Danish timezone (Europe/Copenhagen)
 */
public class DateTimeUtil {
    
    /**
     * Danish timezone (CET/CEST)
     */
    public static final ZoneId DANISH_ZONE = ZoneId.of("Europe/Copenhagen");
    
    /**
     * Get current date/time in Danish timezone
     * @return Current OffsetDateTime in Europe/Copenhagen timezone
     */
    public static OffsetDateTime now() {
        return OffsetDateTime.now(DANISH_ZONE);
    }
    
    /**
     * Get current date/time in Danish timezone plus specified hours
     * @param hours Number of hours to add
     * @return OffsetDateTime in Danish timezone plus hours
     */
    public static OffsetDateTime nowPlusHours(long hours) {
        return now().plusHours(hours);
    }
    
    /**
     * Get current date/time in Danish timezone plus specified days
     * @param days Number of days to add
     * @return OffsetDateTime in Danish timezone plus days
     */
    public static OffsetDateTime nowPlusDays(long days) {
        return now().plusDays(days);
    }
    
    /**
     * Get current date/time in Danish timezone plus specified months
     * @param months Number of months to add
     * @return OffsetDateTime in Danish timezone plus months
     */
    public static OffsetDateTime nowPlusMonths(long months) {
        return now().plusMonths(months);
    }
}

