package in.gov.ipie.service.communication.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;


public interface NotificationLogJpaRepository extends JpaRepository<NotificationLogJpaEntity, UUID>, NotificationLogJpaRepositoryCustom {
}
