CREATE INDEX IF NOT EXISTS idx_schedule_day_active_day_schedule
    ON public.store_order_schedule_day (
    day_of_week,
    store_order_schedule_id
    )
    WHERE deleted_at IS NULL
    AND available = TRUE;