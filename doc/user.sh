echo "{'action': 'insert', 'identifier': 'foo@example.com', 'credentials':'password'}" | curl -d @- "http://localhost:8080/data/users/" | jsonlint; echo ""
