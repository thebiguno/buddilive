Ext.define("BuddiLive.store.transaction.scheduled.ListStore", {
	"extend": "Ext.data.Store",
	"requires": [
		"BuddiLive.model.transaction.scheduled.ListModel"
	],
	"model": "BuddiLive.model.transaction.scheduled.ListModel",
	"autoLoad": true,
	"proxy": {
		"type": "ajax",
		"url": "buddilive/scheduledtransactions.json",
		"reader": {
			"type": "json",
			"root": "data"
		}
	}
});