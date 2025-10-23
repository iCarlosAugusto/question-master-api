-- =============================================================================
-- Migration V9: Setup Supabase Auth Integration for Question Master API
-- =============================================================================
-- This script configures the integration between auth.users (Supabase) and 
-- the public.profiles table
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Add email column to profiles table (useful for caching and queries)
-- -----------------------------------------------------------------------------
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();

-- Create index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_profiles_email ON public.profiles(email);

-- -----------------------------------------------------------------------------
-- 2. Create function to handle new user creation
-- -----------------------------------------------------------------------------
-- This function automatically creates a profile when a user is created in auth.users
-- The profile will inherit the user's ID from Supabase Auth

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
  user_display_name TEXT;
  user_role TEXT;
BEGIN
  -- Extract display name from user metadata or use email prefix
  user_display_name := COALESCE(
    NEW.raw_user_meta_data->>'display_name',
    NEW.raw_user_meta_data->>'name',
    split_part(NEW.email, '@', 1)
  );

  -- Extract role from user metadata, default to 'USER'
  user_role := COALESCE(
    NEW.raw_user_meta_data->>'role',
    'USER'
  );

  -- Ensure role is valid (USER or ADMIN)
  IF user_role NOT IN ('USER', 'ADMIN') THEN
    user_role := 'USER';
  END IF;

  -- Insert new profile with the same ID as auth.users
  INSERT INTO public.profiles (id, role, display_name, email, created_at, updated_at)
  VALUES (
    NEW.id,
    user_role,
    user_display_name,
    NEW.email,
    NOW(),
    NOW()
  )
  ON CONFLICT (id) DO UPDATE SET
    email = EXCLUDED.email,
    updated_at = NOW();

  RETURN NEW;
END;
$$;

-- -----------------------------------------------------------------------------
-- 3. Create trigger to execute function on user creation
-- -----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW
  EXECUTE FUNCTION public.handle_new_user();

-- -----------------------------------------------------------------------------
-- 4. Create function to sync profile updates
-- -----------------------------------------------------------------------------
-- This function updates the profile when user metadata is updated

CREATE OR REPLACE FUNCTION public.handle_user_update()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
  user_display_name TEXT;
  user_role TEXT;
BEGIN
  -- Extract updated display name
  user_display_name := COALESCE(
    NEW.raw_user_meta_data->>'display_name',
    NEW.raw_user_meta_data->>'name',
    split_part(NEW.email, '@', 1)
  );

  -- Extract updated role
  user_role := COALESCE(
    NEW.raw_user_meta_data->>'role',
    'USER'
  );

  -- Ensure role is valid
  IF user_role NOT IN ('USER', 'ADMIN') THEN
    user_role := 'USER';
  END IF;

  -- Update profile
  UPDATE public.profiles
  SET
    display_name = user_display_name,
    role = user_role,
    email = NEW.email,
    updated_at = NOW()
  WHERE id = NEW.id;

  RETURN NEW;
END;
$$;

-- -----------------------------------------------------------------------------
-- 5. Create trigger for user updates
-- -----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS on_auth_user_updated ON auth.users;

CREATE TRIGGER on_auth_user_updated
  AFTER UPDATE ON auth.users
  FOR EACH ROW
  WHEN (
    OLD.raw_user_meta_data IS DISTINCT FROM NEW.raw_user_meta_data 
    OR OLD.email IS DISTINCT FROM NEW.email
  )
  EXECUTE FUNCTION public.handle_user_update();

-- -----------------------------------------------------------------------------
-- 6. Create function to handle user deletion
-- -----------------------------------------------------------------------------
-- This function deletes the profile when a user is deleted from auth.users
-- CASCADE constraints will handle related data (answers, etc.)

CREATE OR REPLACE FUNCTION public.handle_user_delete()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  -- Delete profile (CASCADE will handle related records)
  DELETE FROM public.profiles WHERE id = OLD.id;
  RETURN OLD;
END;
$$;

-- -----------------------------------------------------------------------------
-- 7. Create trigger for user deletion
-- -----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS on_auth_user_deleted ON auth.users;

CREATE TRIGGER on_auth_user_deleted
  BEFORE DELETE ON auth.users
  FOR EACH ROW
  EXECUTE FUNCTION public.handle_user_delete();

-- -----------------------------------------------------------------------------
-- 8. Enable Row Level Security (RLS) on profiles table
-- -----------------------------------------------------------------------------
-- This ensures users can only access/modify their own profiles

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- Drop existing policies to avoid conflicts
-- DROP POLICY IF EXISTS "Users can view own profile" ON public.profiles;
-- DROP POLICY IF EXISTS "Users can update own profile" ON public.profiles;
-- DROP POLICY IF EXISTS "Service can insert profiles" ON public.profiles;
-- DROP POLICY IF EXISTS "Admins can view all profiles" ON public.profiles;
-- DROP POLICY IF EXISTS "Admins can update any profile" ON public.profiles;
-- DROP POLICY IF EXISTS "Admins can delete profiles" ON public.profiles;

-- Policy: Users can view their own profile
CREATE POLICY "Users can view own profile"
  ON public.profiles
  FOR SELECT
  USING (auth.uid() = id);

-- Policy: Users can update their own profile
CREATE POLICY "Users can update own profile"
  ON public.profiles
  FOR UPDATE
  USING (auth.uid() = id);

-- Policy: Service role can insert profiles (used by trigger)
CREATE POLICY "Service can insert profiles"
  ON public.profiles
  FOR INSERT
  WITH CHECK (true);

-- Policy: Admins can view all profiles
CREATE POLICY "Admins can view all profiles"
  ON public.profiles
  FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM public.profiles
      WHERE id = auth.uid() AND role = 'ADMIN'
    )
  );

-- Policy: Admins can update any profile
CREATE POLICY "Admins can update any profile"
  ON public.profiles
  FOR UPDATE
  USING (
    EXISTS (
      SELECT 1 FROM public.profiles
      WHERE id = auth.uid() AND role = 'ADMIN'
    )
  );

-- Policy: Admins can delete profiles
CREATE POLICY "Admins can delete profiles"
  ON public.profiles
  FOR DELETE
  USING (
    EXISTS (
      SELECT 1 FROM public.profiles
      WHERE id = auth.uid() AND role = 'ADMIN'
    )
  );

-- -----------------------------------------------------------------------------
-- 9. Add helpful comments
-- -----------------------------------------------------------------------------
COMMENT ON TABLE public.profiles IS 
'User profiles table that mirrors auth.users from Supabase. IDs are synchronized via triggers.';

COMMENT ON COLUMN public.profiles.id IS 
'User ID - matches the UUID in auth.users from Supabase Auth';

COMMENT ON COLUMN public.profiles.role IS 
'User role: USER or ADMIN';

COMMENT ON COLUMN public.profiles.display_name IS 
'User display name shown in the application';

COMMENT ON COLUMN public.profiles.email IS 
'User email (cached from auth.users for convenience)';

COMMENT ON FUNCTION public.handle_new_user() IS
'Trigger function that automatically creates a profile when a new user is created in auth.users. Extracts metadata from user_metadata.';

COMMENT ON FUNCTION public.handle_user_update() IS
'Trigger function that synchronizes profile data when user metadata is updated in auth.users.';

COMMENT ON FUNCTION public.handle_user_delete() IS
'Trigger function that deletes the profile when a user is deleted from auth.users.';

-- =============================================================================
-- IMPORTANT NOTES:
-- =============================================================================
-- 1. This migration assumes the 'auth' schema exists (managed by Supabase)
-- 2. Profile IDs must match auth.users IDs (UUIDs)
-- 3. User metadata structure expected:
--    {
--      "role": "USER" | "ADMIN",
--      "display_name": "User Name",
--      "name": "Alternative name field"
--    }
-- 4. RLS policies ensure data security at the database level
-- 5. For existing users in auth.users, you may need to manually sync or 
--    re-insert them to trigger the profile creation
-- 6. The triggers handle CREATE, UPDATE, and DELETE operations automatically
-- =============================================================================

