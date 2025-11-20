package dat.entities;

import dat.enums.ActivityLogStatus;
import dat.enums.ActivityLogType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ActivityLog {
    @Id
    @GeneratedValue
    @Column(name = "activity_log_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityLogType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityLogStatus status;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    public ActivityLog(Customer customer, Session session, ActivityLogType type, 
                      ActivityLogStatus status, Map<String, Object> metadata) {
        this.customer = customer;
        this.session = session;
        this.type = type;
        this.status = status;
        this.timestamp = OffsetDateTime.now();
        this.metadata = metadata;
    }
}

