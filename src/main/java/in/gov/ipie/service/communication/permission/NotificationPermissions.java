package in.gov.ipie.service.communication.permission;

/**
 * Permission names for the Notification Log API, enforced via {@code @RequiresPermission}.
 * Business logic must never check role names directly (master standards doc, 5.5) - only these
 * permission constants, resolved from the caller's JWT by common-security.
 */
public final class NotificationPermissions {

    /** Gates GET /api/v1/notifications - who sent what to whom, platform-wide. Granted to SUPER_ADMIN only. */
    public static final String NOTIFICATIONS_VIEW = "NOTIFICATIONS_VIEW";

    private NotificationPermissions() {
    }
}
