package in.gov.ipie.service.communication.repository;

import java.util.Optional;

/** Domain-owned port for the {@code notification_recipients} distribution list. */
public interface NotificationRecipientRepository {

    Optional<String> findEmailByPurpose(String purpose);
}
