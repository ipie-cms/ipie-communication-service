package in.gov.ipie.service.communication.persistence;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.UuidGenerator;

import in.gov.ipie.common.utils.id.UuidV7Generator;

import in.gov.ipie.common.persistence.AuditableJpaEntity;

/** Standard audit + soft-delete columns are inherited from {@link AuditableJpaEntity}. */
@Entity
@Table(name = "notification_recipients")
public class NotificationRecipientJpaEntity extends AuditableJpaEntity {

    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String purpose;

    @Column(nullable = false, length = 254)
    private String email;

    protected NotificationRecipientJpaEntity() {
        // required by JPA
    }

    public String getPurpose() {
        return purpose;
    }

    public String getEmail() {
        return email;
    }
}
