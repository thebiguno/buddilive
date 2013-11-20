Ext.define("BuddiLive.store.transaction.ListStore", {
	"extend": "Ext.data.Store",
	"requires": [
		"BuddiLive.model.transaction.ListModel"
	],
	"model": "BuddiLive.model.transaction.ListModel",
	"pageSize": 250,
	"buffered": true,
	"proxy": {
		"type": "ajax",
		"url": "data/transactions.json",
		"reader": {
			"type": "json",
			"root": "data",
			"totalProperty": "total"
		}
	}
});