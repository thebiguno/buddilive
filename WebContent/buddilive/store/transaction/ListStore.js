Ext.define("BuddiLive.store.transaction.ListStore", {
	"extend": "Ext.data.Store",
	"requires": [
		"BuddiLive.model.transaction.ListModel"
	],
	"model": "BuddiLive.model.transaction.ListModel",
	"proxy": {
		"type": "ajax",
		"url": "gui/transactions.json",
		"reader": {
			"type": "json",
			"root": "data"
		}
	}
});