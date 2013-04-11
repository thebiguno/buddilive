Ext.define("BuddiLive.store.preferences.LocalesComboboxStore", {
	"extend": "Ext.data.Store",
	"model": "BuddiLive.model.shared.Combobox",
	"remoteFilter": false,
	"proxy": {
		"type": "ajax",
		"autoAbort": true, 
		"url": "buddilive/preferences/locales.json",
		"reader": {
			"type": "json",
			"root": "data"
		}
	}
});