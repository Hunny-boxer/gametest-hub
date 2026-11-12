
-- ============================================================
-- GameTest Hub
-- Migration: V1
-- Purpose: Identity, workspace and project foundation
-- ============================================================

-- ------------------------------------------------------------
-- 1. Application users
-- ------------------------------------------------------------

CREATE TABLE app_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    email VARCHAR(320) NOT NULL,
    display_name VARCHAR(120) NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_app_users_email
        CHECK (length(trim(email)) > 0),

    CONSTRAINT chk_app_users_display_name
        CHECK (length(trim(display_name)) > 0)
);

CREATE UNIQUE INDEX uq_app_users_email_lower
    ON app_users (lower(email));


-- ------------------------------------------------------------
-- 2. Workspaces
-- ------------------------------------------------------------

CREATE TABLE workspaces (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name VARCHAR(120) NOT NULL,
    slug VARCHAR(80) NOT NULL UNIQUE,

    created_by UUID NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_workspaces_created_by
        FOREIGN KEY (created_by)
        REFERENCES app_users(id),

    CONSTRAINT chk_workspaces_name
        CHECK (length(trim(name)) > 0),

    CONSTRAINT chk_workspaces_slug
        CHECK (
            slug = lower(slug)
            AND slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'
        )
);


-- ------------------------------------------------------------
-- 3. Workspace memberships
-- ------------------------------------------------------------

CREATE TABLE workspace_members (
    workspace_id UUID NOT NULL,
    user_id UUID NOT NULL,

    role VARCHAR(30) NOT NULL,

    joined_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_workspace_members
        PRIMARY KEY (workspace_id, user_id),

    CONSTRAINT fk_workspace_members_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_workspace_members_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_workspace_members_role
        CHECK (
            role IN (
                'OWNER',
                'ADMIN',
                'QA_LEAD',
                'QA_ENGINEER',
                'VIEWER'
            )
        )
);

CREATE INDEX idx_workspace_members_user
    ON workspace_members(user_id);


-- ------------------------------------------------------------
-- 4. Gaming projects
-- ------------------------------------------------------------

CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    workspace_id UUID NOT NULL,

    name VARCHAR(150) NOT NULL,
    project_key VARCHAR(20) NOT NULL,

    description TEXT,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_by UUID NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_projects_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_projects_created_by
        FOREIGN KEY (created_by)
        REFERENCES app_users(id),

    CONSTRAINT uq_projects_workspace_key
        UNIQUE (workspace_id, project_key),

    CONSTRAINT chk_projects_name
        CHECK (length(trim(name)) > 0),

    CONSTRAINT chk_projects_key
        CHECK (
            project_key ~ '^[A-Z][A-Z0-9_]{1,19}$'
        ),

    CONSTRAINT chk_projects_status
        CHECK (
            status IN ('ACTIVE', 'ARCHIVED')
        )
);

CREATE INDEX idx_projects_workspace_status
    ON projects(workspace_id, status);

CREATE INDEX idx_projects_created_by
    ON projects(created_by);


-- ============================================================
-- End of V1 migration
-- ============================================================
