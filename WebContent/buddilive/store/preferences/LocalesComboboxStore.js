Ext.define("BuddiLive.store.preferences.LocalesComboboxStore", {
	"extend": "Ext.data.Store",
	"model": "BuddiLive.model.shared.Combobox",
	"remoteFilter": false,
	"proxy": {
		"type": "ajax",
		"autoAbort": true, 
		"url": "stores/locales.json",
		"limitParam": null,
		"startParam": null,
		"pageParam": null,
		"reader": {
			"type": "json",
			"root": "data"
		}
	}
});