#Add a new user foo@example.com
echo "[{'identifier': 'foo@example.com', 'credentials':'password'}]" | curl -d @- "http://localhost:8080/data/users/" | jsonlint; echo ""
curl -u "foo@example.com:password" "http://localhost:8080/data/users/" | jsonlint; echo ""
#Add some new sources
echo "[{'name': 'Bank Account 1', 'accountType': 'Savings', 'type':'D', 'startBalance': 12345},{'name': 'Bank Account 2', 'accountType': 'Savings', 'type':'D', 'startBalance': 456},{'name': 'Bank Account 3', 'accountType': 'Chequing', 'type':'D', 'startBalance': 98023},{'name': 'Credit Card', 'accountType': 'Credit Card', 'type': 'C'},{'name':'Groceries','type':'E'},{'name':'Salary','type':'I'},{'name':'Bonus','type':'I'}]" | curl -d @- -u "foo@example.com:password" "http://localhost:8080/data/sources/" | jsonlint; echo ""
curl -u "foo@example.com:password" "http://localhost:8080/data/sources/" | jsonlint; echo ""
#Add a new transaction + split
echo "[{'description': 'Foo', 'date': '2013-01-01', 'splits': [{'parentTransaction': 1, 'amount': '12345', 'fromSource': 1, 'toSource': 2}]}]" | curl -d @- -u "foo@example.com:password" "http://localhost:8080/data/transactions/" | jsonlint; echo ""
curl -u "foo@example.com:password" "http://localhost:8080/data/transactions/" | jsonlint; echo ""