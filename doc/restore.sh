echo '
{
    "accounts": [
        {
            "accountType": "Cash", 
            "name": "Cash in Wallet", 
            "startBalance": "120", 
            "startDate": "1900-01-01", 
            "type": "D", 
            "uuid": "3a26c3ea-f101-4f35-86a7-7b3ba438a79e"
        }, 
        {
            "accountType": "Chequing", 
            "name": "RBC Chequing", 
            "startBalance": "1404", 
            "startDate": "1900-01-01", 
            "type": "D", 
            "uuid": "c29d0f85-da86-4bde-b656-3ba6350f499f"
        }, 
        {
            "accountType": "Credit Card", 
            "name": "Visa Gold", 
            "startBalance": "532", 
            "startDate": "1900-01-01", 
            "type": "C", 
            "uuid": "56a6405f-7f45-4505-a2f5-22d396478e61"
        }
    ], 
    "categories": [
        {
            "categories": [
                {
                    "name": "Bonus", 
                    "parent": "c3231a22-cab8-4e90-955a-219500320900", 
                    "periodType": "MONTH", 
                    "startDate": "1900-01-01T00:00:00", 
                    "type": "I", 
                    "uuid": "6e4b612b-272f-42f0-a280-1701946ebf1e"
                }
            ], 
            "name": "Salary", 
            "periodType": "MONTH", 
            "startDate": "1900-01-01T00:00:00", 
            "type": "I", 
            "uuid": "c3231a22-cab8-4e90-955a-219500320900"
        }, 
        {
            "categories": [
                {
                    "name": "Fuel", 
                    "parent": "138b95ff-4425-4eaa-9997-627c4f230a6a", 
                    "periodType": "MONTH", 
                    "startDate": "1900-01-01T00:00:00", 
                    "type": "E", 
                    "uuid": "d52f3bce-72bc-42fb-a80a-4f9aaef1e95f"
                }, 
                {
                    "name": "Insurance", 
                    "parent": "138b95ff-4425-4eaa-9997-627c4f230a6a", 
                    "periodType": "MONTH", 
                    "startDate": "1900-01-01T00:00:00", 
                    "type": "E", 
                    "uuid": "e06624e0-6221-4574-9579-7f57466416ed"
                }
            ], 
            "name": "Auto", 
            "periodType": "MONTH", 
            "startDate": "1900-01-01T00:00:00", 
            "type": "E", 
            "uuid": "138b95ff-4425-4eaa-9997-627c4f230a6a"
        }, 
        {
            "name": "Groceries", 
            "periodType": "MONTH", 
            "startDate": "1900-01-01T00:00:00", 
            "type": "E", 
            "uuid": "4378588d-e8ef-443b-88f2-81fc9277ac46"
        }
    ], 
    "entries": [
        {
            "amount": "500.00", 
            "category": "4378588d-e8ef-443b-88f2-81fc9277ac46", 
            "date": "2013-03-01"
        }, 
        {
            "amount": "100.00", 
            "category": "d52f3bce-72bc-42fb-a80a-4f9aaef1e95f", 
            "date": "2013-03-01"
        }, 
        {
            "amount": "200.00", 
            "category": "e06624e0-6221-4574-9579-7f57466416ed", 
            "date": "2013-03-01"
        }, 
        {
            "amount": "100.00", 
            "category": "6e4b612b-272f-42f0-a280-1701946ebf1e", 
            "date": "2013-03-01"
        }, 
        {
            "amount": "1000.00", 
            "category": "c3231a22-cab8-4e90-955a-219500320900", 
            "date": "2013-03-01"
        }, 
        {
            "amount": "10.00", 
            "category": "6e4b612b-272f-42f0-a280-1701946ebf1e", 
            "date": "2013-04-01"
        }, 
        {
            "amount": "0.00", 
            "category": "138b95ff-4425-4eaa-9997-627c4f230a6a", 
            "date": "2013-04-01"
        }, 
        {
            "amount": "100.00", 
            "category": "d52f3bce-72bc-42fb-a80a-4f9aaef1e95f", 
            "date": "2013-04-01"
        }, 
        {
            "amount": "600.00", 
            "category": "4378588d-e8ef-443b-88f2-81fc9277ac46", 
            "date": "2013-04-01"
        }
    ], 
    "transactions": [
        {
            "date": "2013-03-10", 
            "deleted": true, 
            "description": "Wal-Mart", 
            "number": "", 
            "splits": [
                {
                    "amount": "42.54", 
                    "from": "3a26c3ea-f101-4f35-86a7-7b3ba438a79e", 
                    "memo": "", 
                    "to": "4378588d-e8ef-443b-88f2-81fc9277ac46"
                }
            ], 
            "uuid": "022fc0f1-309e-4faf-8650-7808707e7e18"
        }, 
        {
            "date": "2013-03-09", 
            "deleted": true, 
            "description": "Esso", 
            "number": "", 
            "splits": [
                {
                    "amount": "10.00", 
                    "from": "3a26c3ea-f101-4f35-86a7-7b3ba438a79e", 
                    "memo": "", 
                    "to": "138b95ff-4425-4eaa-9997-627c4f230a6a"
                }
            ], 
            "uuid": "904957e4-2886-4ae2-9b1a-2ce114aecea7"
        }, 
        {
            "date": "2013-03-08", 
            "deleted": true, 
            "description": "Esso", 
            "number": "", 
            "splits": [
                {
                    "amount": "5.00", 
                    "from": "3a26c3ea-f101-4f35-86a7-7b3ba438a79e", 
                    "memo": "", 
                    "to": "d52f3bce-72bc-42fb-a80a-4f9aaef1e95f"
                }
            ], 
            "uuid": "c017a7e9-64fd-4e47-b3f7-e8115cb2dce1"
        }
    ]
}
' | curl -d @- -u 'foo@example.com:password' http://localhost:8080/data/restore | jsonlint; echo ''
