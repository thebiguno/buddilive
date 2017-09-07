Ext.define("BuddiLive.store.transaction.DescriptionComboboxStore", {
	"extend": "Ext.data.Store",
	"fields": ["value", "text", "style"],
	"remoteFilter": false,
	"proxy": {
		"type": "ajax",
		"autoAbort": true, 
		"url": "data/transactions/descriptions.json",
		"filterParam": null,
		"limitParam": null,
		"startParam": null,
		"pageParam": null,
		"reader": {
			"type": "json",
			"rootProperty": "data"
		}
	}
});