package in.gov.ipie.service.communication.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import in.gov.ipie.common.core.paging.Cursor;
import in.gov.ipie.common.core.paging.CursorPageRequest;
import in.gov.ipie.common.core.paging.CursorPageResult;
import in.gov.ipie.service.communication.domain.NotificationLog;
import in.gov.ipie.service.communication.repository.NotificationLogRepository;

@Repository
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class NotificationLogRepositoryImpl implements NotificationLogRepository {

    private final NotificationLogJpaRepository jpaRepository;

    @Override
    public void record(String purpose, String recipient, String subject, String body, boolean sent, String channel) {
        String status = sent ? "SENT" : "FAILED";
        jpaRepository.save(new NotificationLogJpaEntity(purpose, recipient, subject, body, status, Instant.now(), channel));
    }

    @Override
    public CursorPageResult<NotificationLog> searchAfter(CursorPageRequest pageRequest) {
        Optional<Cursor> cursor = pageRequest.decodeCursor();
        Instant beforeCreatedAt = cursor.map(Cursor::createdAt).orElse(null);
        UUID beforeId = cursor.map(Cursor::id).orElse(null);

        List<NotificationLogJpaEntity> rows = jpaRepository.searchBefore(beforeCreatedAt, beforeId, pageRequest.size() + 1);

        boolean hasMore = rows.size() > pageRequest.size();
        List<NotificationLogJpaEntity> page = hasMore ? rows.subList(0, pageRequest.size()) : rows;
        List<NotificationLog> content = page.stream().map(NotificationLogRepositoryImpl::toDomain).toList();

        String nextCursor = hasMore
                ? new Cursor(page.get(page.size() - 1).getCreatedAt(), page.get(page.size() - 1).getId()).encode()
                : null;

        return CursorPageResult.of(content, nextCursor, hasMore);
    }

    private static NotificationLog toDomain(NotificationLogJpaEntity entity) {
        return new NotificationLog(
                entity.getId(), entity.getPurpose(), entity.getRecipientEmail(), entity.getSubject(), entity.getBody(),
                entity.getStatus(), entity.getChannel(), entity.getSentAt(), entity.getCreatedAt());
    }
}
