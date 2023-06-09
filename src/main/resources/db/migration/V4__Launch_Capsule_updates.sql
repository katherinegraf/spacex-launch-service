DROP TABLE launch_capsule_details;

CREATE TABLE launch_capsule_details (
    launch_id TEXT NOT NULL,
    capsule_id TEXT NOT NULL,
    id SERIAL PRIMARY KEY
);