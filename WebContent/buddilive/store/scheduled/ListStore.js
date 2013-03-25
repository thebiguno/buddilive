Ext.define("BuddiLive.store.scheduled.ListStore", {
	"extend": "Ext.data.Store",
	"requires": [
		"BuddiLive.model.scheduled.ListModel"
	],
	"model": "BuddiLive.model.scheduled.ListModel",
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