Ext.define("BuddiLive.store.transaction.List", {
	"extend": 'Ext.data.Store',
	"config": {
		"fields": [ "id", "amount", "description", "number" ],
		
		"proxy": {
			"type": "ajax",
			"method": "GET",
			"url": "data/transactions.json",
			"reader": {
				"type": "json",
				"rootProperty": "data"
			}
		}
	}
});
