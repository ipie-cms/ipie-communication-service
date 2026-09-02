package in.gov.ipie.service.communication.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.gov.ipie.common.core.paging.CursorPageRequest;
import in.gov.ipie.common.core.paging.CursorPageResult;
import in.gov.ipie.common.security.permission.RequiresPermission;
import in.gov.ipie.common.web.paging.CursorPageResponse;
import in.gov.ipie.service.communication.domain.NotificationLog;
import in.gov.ipie.service.communication.dto.response.NotificationLogResponse;
import in.gov.ipie.service.communication.mapper.NotificationLogApiMapper;
import in.gov.ipie.service.communication.permission.NotificationPermissions;
import in.gov.ipie.service.communication.service.NotificationLogService;

/**
 * Read-only view of every notification this service has ever sent (email or SMS, any recipient) -
 * the platform-operator counterpart to browsing MailHog directly in local dev. Locked to
 * {@code NOTIFICATIONS_VIEW}, granted only to {@code SUPER_ADMIN} (see
 * {@code deploy/keycloak/realm-export.json}) - this is every message sent to every user, not
 * scoped to the caller's own account.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class NotificationLogController {

    private final NotificationLogService notificationLogService;
    private final NotificationLogApiMapper notificationLogApiMapper;

    @GetMapping
    @RequiresPermission(NotificationPermissions.NOTIFICATIONS_VIEW)
    public CursorPageResponse<NotificationLogResponse> listNotifications(
            @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "20") int size) {
        CursorPageRequest pageRequest = new CursorPageRequest(cursor, size);
        CursorPageResult<NotificationLog> result = notificationLogService.listNotifications(pageRequest);
        return CursorPageResponse.from(result, notificationLogApiMapper::toResponse);
    }
}
