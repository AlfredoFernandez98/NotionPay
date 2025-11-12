package dat.dtos;

import dat.enums.ActivityLogStatus;
import dat.enums.ActivityLogType;

import java.time.OffsetDateTime;
import java.util.Map;

public class ActivityLogDTO {
    public Long id;
    public Long customerId;
    public Long sessionId;
    public ActivityLogType type;
    public ActivityLogStatus status;
    public OffsetDateTime timestamp;
    public Map<String, Object> metadata;
}

