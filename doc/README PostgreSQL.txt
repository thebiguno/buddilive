To install Postgres for Buddi Live on a Debian system:

	aptitude install postgresql
	su
	su postgres
	psql
	ALTER ROLE postgres WITH ENCRYPTED PASSWORD 'password';
	Ctrl-D
	exit (you should be root again now)
	psql -h localhost -U postgres
	CREATE USER buddilive WITH PASSWORD 'password';
	CREATE DATABASE buddilive;
	GRANT ALL PRIVILEGES ON DATABASE buddilive to buddilive;
	Ctrl-D
