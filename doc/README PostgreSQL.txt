To install PostgreSQL for Buddi Live on a Debian system (as root),
using a dedicated application schema (recommended):

	aptitude install postgresql
	su postgres
	psql
	ALTER ROLE postgres WITH ENCRYPTED PASSWORD 'password';
	\q
	exit (you should be root again now)

	psql -h localhost -U postgres
	CREATE USER buddilive WITH PASSWORD 'password';
	CREATE DATABASE buddilive ENCODING 'UTF8';
	ALTER DATABASE buddilive OWNER TO buddilive;
	GRANT ALL PRIVILEGES ON DATABASE buddilive TO buddilive;

	\c buddilive
	CREATE SCHEMA buddilive AUTHORIZATION buddilive;
	GRANT USAGE, CREATE ON SCHEMA buddilive TO buddilive;
	ALTER ROLE buddilive IN DATABASE buddilive SET search_path = buddilive, public;
	REVOKE CREATE ON SCHEMA public FROM buddilive;
	\q

Then configure Buddi Live with a PostgreSQL URL that targets the schema:

	db.driver=org.postgresql.Driver
	db.url=jdbc:postgresql://localhost:5432/buddilive?currentSchema=buddilive
	db.user=buddilive
	db.password=password
	db.query=select 1
