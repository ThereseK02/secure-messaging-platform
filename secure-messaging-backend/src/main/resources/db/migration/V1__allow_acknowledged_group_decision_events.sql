DO $$
BEGIN
    IF to_regclass('public.group_decision_events') IS NOT NULL THEN
        ALTER TABLE public.group_decision_events
            DROP CONSTRAINT IF EXISTS
                group_decision_events_event_type_check;

        ALTER TABLE public.group_decision_events
            ADD CONSTRAINT group_decision_events_event_type_check
            CHECK (
                event_type IN (
                    'CREATED',
                    'DISCUSSION_OPENED',
                    'VOTING_OPENED',
                    'VOTE_CAST',
                    'VOTE_CHANGED',
                    'TIE_BREAK_REQUIRED',
                    'QUORUM_NOT_MET',
                    'APPROVED',
                    'REJECTED',
                    'WITHDRAWN',
                    'ACKNOWLEDGED'
                )
            );
    END IF;
END
$$;