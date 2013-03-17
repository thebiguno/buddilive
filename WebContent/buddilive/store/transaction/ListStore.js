Ext.define("BuddiLive.store.transaction.ListStore", {
	"extend": "Ext.data.Store",
	"requires": [
		"BuddiLive.model.transaction.ListModel"
	],
	"model": "BuddiLive.model.transaction.ListModel",
	"pageSize": 50,
	"buffered": true,
	"leadingBufferZone": 200,
	"proxy": {
		"type": "ajax",
		"url": "buddilive/transactions.json",
		"reader": {
			"type": "json",
			"root": "data",
			"totalProperty": "total"
		}
	}
});