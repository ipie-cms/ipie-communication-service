-- ipie-communication-service schema.
--
-- A baseline: it declares the schema as it stands rather than replaying how it was reached. It
-- supersedes the 11 migrations that preceded the repository's first commit, whose intermediate
-- states existed only on development machines.

-- pgcrypto is created but never commented on. pg_dump emits a COMMENT ON EXTENSION beside the
-- CREATE, and commenting on an extension requires owning it - which the service's migration role
-- does not, and should not. CREATE EXTENSION IF NOT EXISTS is a no-op when the extension is already
-- installed, so it stays: it documents the dependency and works on a fresh database provisioned by
-- someone who does have the privilege.
CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;

CREATE TABLE public.notification_log (
    id uuid NOT NULL,
    purpose character varying(50) NOT NULL,
    recipient_email character varying(254) NOT NULL,
    subject character varying(255) NOT NULL,
    status character varying(20) NOT NULL,
    sent_at timestamp with time zone DEFAULT now() NOT NULL,
    created_at timestamp with time zone NOT NULL,
    created_by character varying(100) NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    updated_by character varying(100) NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    deleted_at timestamp with time zone,
    deleted_by character varying(100),
    channel character varying(20) DEFAULT 'EMAIL'::character varying NOT NULL,
    body text,
    CONSTRAINT chk_notification_log_status CHECK (((status)::text = ANY ((ARRAY['SENT'::character varying, 'FAILED'::character varying])::text[])))
);

CREATE TABLE public.notification_recipients (
    id uuid NOT NULL,
    purpose character varying(50) NOT NULL,
    email character varying(254) NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(100) NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    updated_by character varying(100) NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    deleted_at timestamp with time zone,
    deleted_by character varying(100)
);

CREATE TABLE public.outbox_events (
    event_id character varying(64) NOT NULL,
    payload text NOT NULL,
    occurred_at timestamp with time zone NOT NULL,
    published_at timestamp with time zone
);

CREATE TABLE public.processed_events (
    event_id character varying(128) NOT NULL,
    processed_at timestamp with time zone DEFAULT now() NOT NULL
);

ALTER TABLE ONLY public.notification_log
    ADD CONSTRAINT notification_log_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.notification_recipients
    ADD CONSTRAINT notification_recipients_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.outbox_events
    ADD CONSTRAINT outbox_events_pkey PRIMARY KEY (event_id);

ALTER TABLE ONLY public.processed_events
    ADD CONSTRAINT processed_events_pkey PRIMARY KEY (event_id);

CREATE INDEX idx_notification_recipients_purpose ON public.notification_recipients USING btree (purpose);

CREATE INDEX idx_outbox_events_unpublished ON public.outbox_events USING btree (occurred_at) WHERE (published_at IS NULL);
