CREATE TABLE public.user_subscriptions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    exam_id BIGSERIAL NOT NULL,
    plan VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    stripe_customer_id VARCHAR(255) NOT NULL,
    stripe_subscription_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user_subscription_user
        FOREIGN KEY (user_id) REFERENCES public.profiles(id),

    CONSTRAINT fk_user_subscription_exam
        FOREIGN KEY (exam_id) REFERENCES public.exams(id)
);
