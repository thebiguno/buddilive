Ext.define("BuddiLive.store.preferences.CurrenciesComboboxStore", {
	"extend": "Ext.data.Store",
	"model": "BuddiLive.model.shared.Combobox",
	"remoteFilter": false,
	"proxy": {
		"type": "ajax",
		"autoAbort": true, 
		"url": "buddilive/preferences/currencies.json",
		"reader": {
			"type": "json",
			"root": "data"
		}
	}
});