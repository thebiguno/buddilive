To install Postgres for Buddi Live on a Debian system (as root):

	aptitude install postgresql
	su postgres
	psql
	ALTER ROLE postgres WITH ENCRYPTED PASSWORD 'password';
	Ctrl-D
	exit (you should be root again now)
	psql -h localhost -U postgres
	CREATE USER buddilive WITH PASSWORD 'password';
	CREATE DATABASE buddilive ENCODING 'UTF8';
	GRANT ALL PRIVILEGES ON DATABASE buddilive to buddilive;
	Ctrl-D
