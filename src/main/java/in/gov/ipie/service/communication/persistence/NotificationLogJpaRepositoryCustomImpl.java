package in.gov.ipie.service.communication.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * {@code Impl}-suffixed so Spring Data JPA's repository factory auto-detects this as the fragment
 * backing {@link NotificationLogJpaRepositoryCustom} - same mechanism as
 * {@code UserJpaRepositoryCustomImpl} (ipie-user-service), which this mirrors. Orders
 * {@code (createdAt, id)} DESC rather than that example's ASC - a notification feed reads
 * most-recently-sent-first, unlike a stable user listing.
 */
class NotificationLogJpaRepositoryCustomImpl implements NotificationLogJpaRepositoryCustom {

    private final EntityManager entityManager;

    NotificationLogJpaRepositoryCustomImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<NotificationLogJpaEntity> searchBefore(Instant beforeCreatedAt, UUID beforeId, int limit) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<NotificationLogJpaEntity> query = cb.createQuery(NotificationLogJpaEntity.class);
        Root<NotificationLogJpaEntity> root = query.from(NotificationLogJpaEntity.class);

        Predicate keyset = keysetPredicate(cb, root, beforeCreatedAt, beforeId);
        if (keyset != null) {
            query.where(keyset);
        }
        query.orderBy(cb.desc(root.get("createdAt")), cb.desc(root.get("id")));

        return entityManager.createQuery(query)
                .setMaxResults(limit)
                .getResultList();
    }

    private static Predicate keysetPredicate(
            CriteriaBuilder cb, Root<NotificationLogJpaEntity> root, Instant beforeCreatedAt, UUID beforeId) {
        if (beforeCreatedAt == null || beforeId == null) {
            return null;
        }
        return cb.or(
                cb.lessThan(root.get("createdAt"), beforeCreatedAt),
                cb.and(cb.equal(root.get("createdAt"), beforeCreatedAt), cb.lessThan(root.get("id"), beforeId)));
    }
}
