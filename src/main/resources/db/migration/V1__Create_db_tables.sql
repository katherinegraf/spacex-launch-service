DROP TABLE IF EXISTS capsules;
DROP TABLE IF EXISTS launchpads CASCADE;
DROP TABLE IF EXISTS launches CASCADE;
DROP TABLE IF EXISTS payloads;
DROP TABLE IF EXISTS launch_failures;
DROP TABLE IF EXISTS launch_capsule_details;

CREATE TABLE capsules (
	capsule_id TEXT PRIMARY KEY NOT NULL,
	serial_name TEXT NOT NULL,
	status TEXT NOT NULL,
	last_update TEXT,
	water_landings INTEGER,
	land_landings INTEGER,
	type TEXT NOT NULL
);

CREATE TABLE launchpads (
	launchpad_id TEXT PRIMARY KEY NOT NULL,
	full_name TEXT NOT NULL,
	locality TEXT NOT NULL,
	region TEXT NOT NULL,
	status TEXT NOT NULL,
	details TEXT NOT NULL,
	launch_attempts INTEGER NOT NULL,
	launch_successes INTEGER NOT NULL
);

CREATE TABLE launches (
	launch_id TEXT PRIMARY KEY NOT NULL,
	name TEXT NOT NULL,
	details TEXT,
	date_utc TEXT NOT NULL,
	success BOOLEAN,
	launchpad_id TEXT NOT NULL,
	updated_at DATE,
	CONSTRAINT fk_launchpad
	    FOREIGN KEY(launchpad_id)
	        REFERENCES launchpads(launchpad_id)
);

CREATE TABLE launch_failures (
	id SERIAL PRIMARY KEY NOT NULL,
	time INTEGER NOT NULL,
	altitude INTEGER,
	reason TEXT NOT NULL,
	launch_id TEXT NOT NULL,
	CONSTRAINT fk_launch
	    FOREIGN KEY(launch_id)
	        REFERENCES launches(launch_id)
);

CREATE TABLE payloads (
	payload_id TEXT PRIMARY KEY NOT NULL,
	name TEXT NOT NULL,
	type TEXT NOT NULL,
	regime TEXT,
	customers TEXT NOT NULL,
	nationalities TEXT NOT NULL,
	manufacturers TEXT NOT NULL,
	mass_kg FLOAT NOT NULL,
	mass_lbs FLOAT NOT NULL,
	launch_id TEXT NOT NULL,
	CONSTRAINT fk_launch
	    FOREIGN KEY(launch_id)
	        REFERENCES launches(launch_id)
);


CREATE TABLE launch_capsule_details (
	launch_id TEXT NOT NULL,
	capsule_id TEXT NOT NULL,
    id SERIAL PRIMARY KEY
);
