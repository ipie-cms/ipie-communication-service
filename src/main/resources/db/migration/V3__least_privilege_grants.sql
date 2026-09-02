-- Grants ipie-communication-service's runtime role exactly what it needs, and nothing else.
-- ARCHITECTURE_WORKING_PLAN.md Stage 5, item 1. Mirrors ipie-user-service's V30 and ipie-iam-service's V17.
--
-- The roles are created by the deployment, not here:
-- ipie-platform-mca/deploy/postgres/roles/02-create-service-roles.sql. This migration is guarded on
-- their existence and is a no-op without them, so a developer running against `postgres` sees no
-- change.
--
-- WHY AN EXPLICIT LIST, WHEN THIS SERVICE OWNS THE WHOLE DATABASE
--
-- Unlike the shared user/iam database, "every table here is mine" is true, so a blanket grant would
-- not be wrong. The list is still written out for the two tables that would otherwise be swept in:
-- flyway_schema_history, which only the owner role that runs migrations has any business writing,
-- and any future table added by a migration that has not thought about its own grants. An explicit
-- list makes adding one a deliberate act, visible in a diff.
--
-- What this database holds is why it is worth the care: notification_log carries delivered message
-- bodies and recipient addresses - other people's personal data, and the content sent to them.

DO $$
DECLARE
    owned_tables text[] := ARRAY[
        'notification_log',
        'notification_recipients',
        'outbox_events',
        'processed_events'
        -- flyway_schema_history deliberately absent - owner-only.
    ];
    target text;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'ipie_communication_service_app') THEN
        RAISE NOTICE 'ipie_communication_service_app does not exist - skipping grants (local development)';
        RETURN;
    END IF;

    GRANT USAGE ON SCHEMA public TO ipie_communication_service_app;

    FOREACH target IN ARRAY owned_tables LOOP
        IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname = 'public' AND tablename = target) THEN
            EXECUTE format(
                'GRANT SELECT, INSERT, UPDATE, DELETE ON public.%I TO ipie_communication_service_app', target);
        ELSE
            RAISE WARNING 'V12: table % is listed but does not exist - grant skipped', target;
        END IF;
    END LOOP;

    -- No TRUNCATE: the notification log is the record of what was sent to whom, and the running
    -- application has no reason to be able to empty it in one statement.

    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'ipie_communication_service_owner') THEN
        ALTER DEFAULT PRIVILEGES FOR ROLE ipie_communication_service_owner IN SCHEMA public
            GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO ipie_communication_service_app;
    END IF;
END
$$;
