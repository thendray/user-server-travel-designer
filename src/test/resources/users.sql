CREATE TABLE users (
	id serial4 NOT NULL,
	email varchar(255) NOT NULL,
	username varchar(255) NOT NULL,
	"password" varchar(255) NOT NULL,
	profile_photo text NULL,
	CONSTRAINT users_pkey PRIMARY KEY (id)
);