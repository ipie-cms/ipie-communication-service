package in.gov.ipie.service.communication.repository;

import in.gov.ipie.common.core.paging.CursorPageRequest;
import in.gov.ipie.common.core.paging.CursorPageResult;
import in.gov.ipie.service.communication.domain.NotificationLog;

/** Domain-owned port for the append-only {@code notification_log} audit trail. */
public interface NotificationLogRepository {

    /**
     * {@code recipient} is an email address when {@code channel} is {@code "EMAIL"}, a phone
     * number when {@code "SMS"} - the column is a generic recipient identifier, not
     * email-specific, despite its historical {@code recipient_email} name. {@code body} is the
     * content actually persisted for later viewing (master standards doc, DPDP masking rule) -
     * callers must mask any embedded secret (e.g. a verification token) themselves before calling
     * this; nothing here re-checks that.
     */
    void record(String purpose, String recipient, String subject, String body, boolean sent, String channel);

    /**
     * Keyset ("seek") listing, most-recently-sent first - this table is expected to grow large
     * and high-traffic (every notification this platform ever sends), so an offset/COUNT(*)
     * listing was never on the table (master standards doc, section 8).
     */
    CursorPageResult<NotificationLog> searchAfter(CursorPageRequest pageRequest);
}
