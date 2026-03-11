CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    otp_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    totp_secret_encrypted VARCHAR(255),
    totp_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE applicant_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    national_id_number_encrypted VARCHAR(255) NOT NULL,
    national_id_hash VARCHAR(64) NOT NULL UNIQUE,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    date_of_birth_encrypted VARCHAR(255),
    gender_encrypted VARCHAR(255),
    phone_encrypted VARCHAR(255),
    address_line_encrypted VARCHAR(255),
    province VARCHAR(80),
    district VARCHAR(80),
    school_name VARCHAR(150),
    grade VARCHAR(10),
    option_attended VARCHAR(20),
    completion_year INTEGER,
    nida_verified BOOLEAN NOT NULL DEFAULT FALSE,
    nida_verified_at TIMESTAMP WITH TIME ZONE,
    nesa_verified BOOLEAN NOT NULL DEFAULT FALSE,
    nesa_verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE applications (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES applicant_profiles(id),
    application_number VARCHAR(40) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    reviewed_by_user_id UUID REFERENCES users(id),
    rejection_reason VARCHAR(500),
    decision_note VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE documents (
    id UUID PRIMARY KEY,
    application_id UUID REFERENCES applications(id),
    type VARCHAR(30) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    storage_key VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    uploaded_by_user_id UUID NOT NULL REFERENCES users(id)
);

CREATE TABLE application_status_history (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications(id),
    old_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    reason VARCHAR(500),
    changed_by_user_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID REFERENCES users(id),
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id VARCHAR(80) NOT NULL,
    metadata_json TEXT,
    ip_address VARCHAR(80),
    user_agent VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_application_submitted_at ON applications (submitted_at DESC);
CREATE INDEX idx_application_status ON applications (status);
CREATE INDEX idx_profile_name ON applicant_profiles (last_name, first_name);
CREATE INDEX idx_profile_national_id_hash ON applicant_profiles (national_id_hash);
CREATE INDEX idx_history_application_created_at ON application_status_history (application_id, created_at DESC);
CREATE INDEX idx_documents_application ON documents (application_id);
CREATE INDEX idx_audit_created_at ON audit_logs (created_at DESC);
