To use Buddi in standalone mode, simply set the config.propertie to use Derby, and do 
not include a URL (i.e have only one line in the file):

db.driver=org.apache.derby.jdbc.EmbeddedDriver

By not specifiying the URL, Buddi will automatically create the database in your user
folder (OS specific; on Windows this will be 'c:\Documents and Settings\<user>\Application Data\BuddiLive';
in Linux it will be .buddilive, and in OSX it will be ~/<user>/Library/Application Support/BuddiLive).

To connect to the Derby DB for troubleshooting or other use, please do the following (assuming *nix system, 
you may have to adapt for Windows use) from the checked out source directory:

	java -jar lib/derbyrun.jar ij
	# after you get into the Derby shell, connect to the DB
	CONNECT 'jdbc:derby:/Path/to/BuddiLive/derby';
	
for instance, on Linux you would do something like
	CONNECT 'jdbc:derby:/home/user/.buddilive/derby';
