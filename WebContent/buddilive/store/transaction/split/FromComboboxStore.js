Ext.define("BuddiLive.store.transaction.split.FromComboboxStore", {
	"extend": "Ext.data.Store",
	"fields": ["value", "text", "style"],
	"remoteFilter": false,
	"proxy": {
		"type": "ajax",
		"autoAbort": true, 
		"url": "data/sources/from.json",
		"limitParam": null,
		"startParam": null,
		"pageParam": null,
		"reader": {
			"type": "json",
			"rootProperty": "data"
		}
	}
});