-- Create user roles enum
-- CREATE TYPE app_role AS ENUM ('USER', 'ADMIN');

-- Create profiles table (mirrors auth.users from Supabase)
CREATE TABLE public.profiles (
    id UUID PRIMARY KEY,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    display_name TEXT,
    email VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
