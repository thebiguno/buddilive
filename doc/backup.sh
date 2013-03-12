RESTORE=doc/restore.sh
echo "echo '" > $RESTORE
curl -u "wyatt.olson@gmail.com:password" "http://localhost:8080/data/backup" | jsonlint >> $RESTORE
echo "' | curl -d @- -u 'wyatt.olson@gmail.com:password' http://localhost:8080/data/restore | jsonlint; echo ''" >> $RESTORE
