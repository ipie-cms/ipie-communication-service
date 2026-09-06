-- Notification recipients: which address receives each kind of message.
--
-- This is configuration the platform reads at runtime through findEmailByPurpose, not sample
-- content - the approval request for a new registration goes wherever this table says, and reading
-- the sending code tells you nothing about who receives it.
--
-- The address below is the development value, matching the administrator in the checked-in Keycloak
-- realm fixture. Every deployed environment must replace it with the real mailbox for that purpose
-- before registrations are processed, or approval requests will be sent to an address nobody reads.

INSERT INTO notification_recipients (id, purpose, email, created_at, created_by, updated_at, updated_by)
VALUES ('50000000-0000-0000-0000-000000000001', 'USER_VERIFICATION_REQUEST', 'admin@ipie.gov.in',
        now(), 'flyway-seed', now(), 'flyway-seed');
