CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS public.time_slots (
    time_slot_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    start_time   TIMESTAMP NOT NULL,
    end_time     TIMESTAMP NOT NULL,
    user_id      UUID NOT NULL,
    CHECK (end_time > start_time)
);

CREATE TABLE IF NOT EXISTS public.time_slot_state (
    time_slot_id UUID PRIMARY KEY REFERENCES public.time_slots(time_slot_id) ON DELETE CASCADE,
    state        VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.meetings (
    time_slot_id UUID PRIMARY KEY REFERENCES public.time_slots(time_slot_id) ON DELETE CASCADE,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    participants TEXT
);

CREATE INDEX IF NOT EXISTS idx_time_slots_user_id ON public.time_slots(user_id);
CREATE INDEX IF NOT EXISTS idx_time_slot_state_state ON public.time_slot_state(state);
       