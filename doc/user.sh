echo "{'action': 'insert', 'identifier': 'wyatt.olson@gmail.com', 'credentials':'password'}" | curl -d @- "http://localhost:8080/data/users" | jsonlint; echo ""
