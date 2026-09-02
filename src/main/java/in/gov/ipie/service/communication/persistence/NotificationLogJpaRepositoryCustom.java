package in.gov.ipie.service.communication.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Keyset ("seek") query fragment for {@link NotificationLogJpaRepository} - see the impl for the Criteria API query. */
public interface NotificationLogJpaRepositoryCustom {

    /**
     * Rows strictly before {@code (beforeCreatedAt, beforeId)} in {@code (createdAt DESC, id DESC)}
     * order - {@code null}/{@code null} for the first page. Fetch {@code limit} rows; the caller
     * decides paging metadata from however many actually came back.
     */
    List<NotificationLogJpaEntity> searchBefore(Instant beforeCreatedAt, UUID beforeId, int limit);
}
