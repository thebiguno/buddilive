Ext.define("BuddiLive.store.scheduled.ListStore", {
	"extend": "Ext.data.Store",
	"requires": [],
	"autoLoad": true,
	"fields": ["name"],
	"proxy": {
		"type": "ajax",
		"url": "data/scheduledtransactions.json",
		"reader": {
			"type": "json",
			"rootProperty": "data"
		}
	}
});