#Add a new user foo@example.com
echo "[{'identifier': 'foo@example.com', 'credentials':'password'}]" | curl -d @- "http://localhost:8080/data/users/" | jsonlint; echo ""
curl -u "foo@example.com:password" "http://localhost:8080/data/users/" | jsonlint; echo ""
#Add some new sources
echo "[{'name': 'Bank Account 1', 'type':'D', 'startBalance': 12345},{'name': 'Credit Card', 'type': 'C'},{'name':'Groceries','type':'E'},{'name':'Salary','type':'I'},{'name':'Bonus','type':'I', 'parent': 4}]" | curl -d @- -u "foo@example.com:password" "http://localhost:8080/data/sources/" | jsonlint; echo ""
curl -u "foo@example.com:password" "http://localhost:8080/data/sources/" | jsonlint; echo ""