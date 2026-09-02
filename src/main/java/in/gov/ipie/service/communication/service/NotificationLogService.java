package in.gov.ipie.service.communication.service;

import in.gov.ipie.common.core.paging.CursorPageRequest;
import in.gov.ipie.common.core.paging.CursorPageResult;
import in.gov.ipie.service.communication.domain.NotificationLog;

/**
 * Read side of the {@code notification_log} audit trail - kept separate from
 * {@link NotificationService} (which only orchestrates sending) so that interface stays focused
 * on "who gets notified about what," not query concerns.
 */
public interface NotificationLogService {

    CursorPageResult<NotificationLog> listNotifications(CursorPageRequest pageRequest);
}
