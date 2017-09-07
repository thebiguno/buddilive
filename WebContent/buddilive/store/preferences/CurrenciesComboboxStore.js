Ext.define("BuddiLive.store.preferences.CurrenciesComboboxStore", {
	"extend": "Ext.data.Store",
	"fields": ["value", "text", "style"],
	"remoteFilter": false,
	"proxy": {
		"type": "ajax",
		"autoAbort": true, 
		"url": "stores/currencies.json",
		"limitParam": null,
		"startParam": null,
		"pageParam": null,
		"reader": {
			"type": "json",
			"rootProperty": "data"
		}
	}
});