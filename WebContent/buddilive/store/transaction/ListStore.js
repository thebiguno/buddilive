Ext.define("BuddiLive.store.transaction.ListStore", {
	"extend": "Ext.data.BufferedStore",
	"requires": [],
	"fields": ["date", "description", "number", "deleted", "splits"],
	"pageSize": 250,
	"buffered": true,
	"proxy": {
		"type": "ajax",
		"url": "data/transactions.json",
		"reader": {
			"type": "json",
			"rootProperty": "data",
			"totalProperty": "total"
		}
	}
});