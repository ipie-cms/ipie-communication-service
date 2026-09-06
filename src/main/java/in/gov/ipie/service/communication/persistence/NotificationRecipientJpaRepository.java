package in.gov.ipie.service.communication.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;


public interface NotificationRecipientJpaRepository extends JpaRepository<NotificationRecipientJpaEntity, UUID> {

    Optional<NotificationRecipientJpaEntity> findFirstByPurpose(String purpose);
}
