To create the Derby DB for standalone use, please do the following (assuming *nix system, you may have to adapt for Windows use):

			rm -rf db; echo "CONNECT 'jdbc:derby:db/buddi;create=true';" | java -jar lib/derbyrun.jar ij
