echo '
{
    "accounts": [
        {
            "accountType": "Cash", 
            "name": "My Wallet", 
            "startBalance": "120.00", 
            "startDate": "2013-03-11", 
            "type": "D", 
            "uuid": "AccountImpl-13d5c65dd62-224-3b69"
        }, 
        {
            "accountType": "Chequing", 
            "name": "TD Chequing", 
            "startBalance": "3012.42", 
            "startDate": "2013-03-03", 
            "type": "D", 
            "uuid": "AccountImpl-13d5c695c01-7de8-1251"
        }, 
        {
            "accountType": "Credit Card", 
            "name": "Visa", 
            "startBalance": "0.00", 
            "startDate": "2013-03-11", 
            "type": "C", 
            "uuid": "AccountImpl-13d5c698d77-d029-f117"
        }
    ], 
    "categories": [
        {
            "name": "Bonus", 
            "periodType": "MONTH", 
            "type": "I", 
            "uuid": "BudgetCategoryImpl-13d5c659e0b-b66a-45dd"
        }, 
        {
            "name": "Investment Income", 
            "periodType": "MONTH", 
            "type": "I", 
            "uuid": "BudgetCategoryImpl-13d5c659e0b-94ce-1108"
        }, 
        {
            "name": "Salary", 
            "periodType": "MONTH", 
            "type": "I", 
            "uuid": "BudgetCategoryImpl-13d5c659e0b-952e-b78b"
        }, 
        {
            "name": "Auto", 
            "periodType": "MONTH", 
            "type": "E", 
            "uuid": "BudgetCategoryImpl-13d5c659e0a-481f-b7c9"
        }, 
        {
            "name": "Entertainment", 
            "periodType": "MONTH", 
            "type": "E", 
            "uuid": "BudgetCategoryImpl-13d5c659e0a-dd7a-1fc3"
        }, 
        {
            "name": "Groceries", 
            "periodType": "MONTH", 
            "type": "E", 
            "uuid": "BudgetCategoryImpl-13d5c659e0a-8e20-e5c"
        }, 
        {
            "name": "Household", 
            "periodType": "MONTH", 
            "type": "E", 
            "uuid": "BudgetCategoryImpl-13d5c659e0a-cb-662c"
        }, 
        {
            "name": "Investment Expenses", 
            "periodType": "MONTH", 
            "type": "E", 
            "uuid": "BudgetCategoryImpl-13d5c659e0a-60e1-be7f"
        }, 
        {
            "name": "Misc. Expenses", 
            "periodType": "MONTH", 
            "type": "E", 
            "uuid": "BudgetCategoryImpl-13d5c659e0a-d2a0-9e81"
        }, 
        {
            "name": "Utilities", 
            "periodType": "MONTH", 
            "type": "E", 
            "uuid": "BudgetCategoryImpl-13d5c659e0a-bf50-50d8"
        }
    ], 
    "entries": [
        {
            "amount": "100.00", 
            "category": "BudgetCategoryImpl-13d5c659e0b-b66a-45dd", 
            "date": "2013-03-01"
        }, 
        {
            "amount": "500.00", 
            "category": "BudgetCategoryImpl-13d5c659e0b-b66a-45dd", 
            "date": "2013-04-01"
        }, 
        {
            "amount": "200.00", 
            "category": "BudgetCategoryImpl-13d5c659e0b-94ce-1108", 
            "date": "2013-04-01"
        }, 
        {
            "amount": "1000.00", 
            "category": "BudgetCategoryImpl-13d5c659e0b-952e-b78b", 
            "date": "2013-03-01"
        }, 
        {
            "amount": "800.00", 
            "category": "BudgetCategoryImpl-13d5c659e0b-952e-b78b", 
            "date": "2013-04-01"
        }, 
        {
            "amount": "200.00", 
            "category": "BudgetCategoryImpl-13d5c659e0a-481f-b7c9", 
            "date": "2013-03-01"
        }, 
        {
            "amount": "10.00", 
            "category": "BudgetCategoryImpl-13d5c659e0a-dd7a-1fc3", 
            "date": "2013-03-01"
        }, 
        {
            "amount": "500.00", 
            "category": "BudgetCategoryImpl-13d5c659e0a-8e20-e5c", 
            "date": "2013-03-01"
        }, 
        {
            "amount": "400.00", 
            "category": "BudgetCategoryImpl-13d5c659e0a-8e20-e5c", 
            "date": "2013-04-01"
        }, 
        {
            "amount": "300.00", 
            "category": "BudgetCategoryImpl-13d5c659e0a-d2a0-9e81", 
            "date": "2013-03-01"
        }, 
        {
            "amount": "300.00", 
            "category": "BudgetCategoryImpl-13d5c659e0a-d2a0-9e81", 
            "date": "2013-04-01"
        }, 
        {
            "amount": "90.00", 
            "category": "BudgetCategoryImpl-13d5c659e0a-bf50-50d8", 
            "date": "2013-03-01"
        }, 
        {
            "amount": "90.00", 
            "category": "BudgetCategoryImpl-13d5c659e0a-bf50-50d8", 
            "date": "2013-04-01"
        }
    ], 
    "transactions": [
        {
            "date": "2013-04-10", 
            "description": "Test", 
            "number": "", 
            "splits": [
                {
                    "amount": "12314.00", 
                    "from": "BudgetCategoryImpl-13d5c659e0b-b66a-45dd", 
                    "memo": "", 
                    "to": "AccountImpl-13d5c65dd62-224-3b69"
                }
            ], 
            "uuid": "871396bd-ed44-4f64-824d-654399171fae"
        }, 
        {
            "date": "2013-03-11", 
            "deleted": true, 
            "description": "Co-op", 
            "number": "", 
            "splits": [
                {
                    "amount": "102.12", 
                    "from": "AccountImpl-13d5c65dd62-224-3b69", 
                    "memo": "", 
                    "to": "BudgetCategoryImpl-13d5c659e0a-8e20-e5c"
                }
            ], 
            "uuid": "TransactionImpl-13d5c690e68-1908-484e"
        }, 
        {
            "date": "2013-03-11", 
            "deleted": true, 
            "description": "Mortgage Payment", 
            "number": "", 
            "splits": [
                {
                    "amount": "2000.00", 
                    "from": "AccountImpl-13d5c695c01-7de8-1251", 
                    "memo": "", 
                    "to": "BudgetCategoryImpl-13d5c659e0a-cb-662c"
                }
            ], 
            "uuid": "TransactionImpl-13d5c6b0330-363f-ea9c"
        }, 
        {
            "date": "2013-03-11", 
            "deleted": true, 
            "description": "Foo", 
            "number": "", 
            "splits": [
                {
                    "amount": "123.45", 
                    "from": "AccountImpl-13d5c65dd62-224-3b69", 
                    "memo": "", 
                    "to": "BudgetCategoryImpl-13d5c659e0a-dd7a-1fc3"
                }
            ], 
            "uuid": "4507645c-dbb1-4a31-827d-d3bd143d93f7"
        }, 
        {
            "date": "2013-03-05", 
            "deleted": true, 
            "description": "Cash Withdrawl", 
            "number": "", 
            "splits": [
                {
                    "amount": "100.00", 
                    "from": "AccountImpl-13d5c695c01-7de8-1251", 
                    "memo": "", 
                    "to": "AccountImpl-13d5c65dd62-224-3b69"
                }
            ], 
            "uuid": "b4d94b44-2868-4319-9828-5368746f64b1"
        }, 
        {
            "date": "2013-03-03", 
            "deleted": true, 
            "description": "Cash Withdrawl", 
            "number": "", 
            "splits": [
                {
                    "amount": "500.00", 
                    "from": "AccountImpl-13d5c695c01-7de8-1251", 
                    "memo": "", 
                    "to": "AccountImpl-13d5c65dd62-224-3b69"
                }
            ], 
            "uuid": "TransactionImpl-13d5c6a8a84-547d-a1f"
        }, 
        {
            "date": "2013-03-01", 
            "deleted": true, 
            "description": "Foo", 
            "number": "", 
            "splits": [
                {
                    "amount": "200.52", 
                    "from": "AccountImpl-13d5c65dd62-224-3b69", 
                    "memo": "", 
                    "to": "BudgetCategoryImpl-13d5c659e0a-dd7a-1fc3"
                }
            ], 
            "uuid": "TransactionImpl-13d5c6a0d72-b211-cf04"
        }, 
        {
            "date": "2013-02-28", 
            "deleted": true, 
            "description": "Cash Withdrawl", 
            "number": "", 
            "splits": [
                {
                    "amount": "250.00", 
                    "from": "AccountImpl-13d5c695c01-7de8-1251", 
                    "memo": "", 
                    "to": "AccountImpl-13d5c65dd62-224-3b69"
                }
            ], 
            "uuid": "6932b816-3436-462c-a7a7-7f5ee697c7c8"
        }
    ]
}
' | curl -d @- -u 'wyatt.olson@gmail.com:password' http://localhost:8080/data/restore | jsonlint; echo ''
