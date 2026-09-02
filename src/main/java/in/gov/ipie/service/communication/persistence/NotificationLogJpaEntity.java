package in.gov.ipie.service.communication.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.UuidGenerator;

import in.gov.ipie.common.utils.id.UuidV7Generator;

import in.gov.ipie.common.persistence.AuditableJpaEntity;

/**
 * Append-only audit trail - written once via {@code NotificationLogRepositoryImpl.record}, never
 * updated. Standard audit + soft-delete columns are inherited from {@link AuditableJpaEntity} for
 * platform-wide consistency even though this table is never updated after insert.
 */
@Entity
@Table(name = "notification_log")
public class NotificationLogJpaEntity extends AuditableJpaEntity {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String purpose;

    // Holds an email address or a phone number depending on channel - see channel below. Name kept
    // as-is (not renamed) to keep this migration additive; genuinely generic despite the name.
    @Column(name = "recipient_email", nullable = false, length = 254)
    private String recipientEmail;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @Column(nullable = false, length = 20)
    private String channel;

    // Nullable: historical rows sent before this column existed have none (V10, expand-only
    // migration). Callers must mask any embedded secret before this is set - see
    // NotificationServiceImpl.sendVerificationRequest.
    @Column(columnDefinition = "TEXT")
    private String body;

    protected NotificationLogJpaEntity() {
        // required by JPA
    }

    public NotificationLogJpaEntity(
            String purpose, String recipientEmail, String subject, String body, String status, Instant sentAt, String channel) {
        this.purpose = purpose;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.body = body;
        this.status = status;
        this.sentAt = sentAt;
        this.channel = channel;
    }

    public UUID getId() {
        return id;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getSubject() {
        return subject;
    }

    public String getStatus() {
        return status;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public String getChannel() {
        return channel;
    }

    public String getBody() {
        return body;
    }
}
