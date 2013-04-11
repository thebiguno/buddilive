Ext.define("BuddiLive.store.preferences.TimezonesComboboxStore", {
	"extend": "Ext.data.Store",
	"model": "BuddiLive.model.shared.Combobox",
	"remoteFilter": false,
	"proxy": {
		"type": "ajax",
		"autoAbort": true, 
		"url": "buddilive/preferences/timezones.json",
		"reader": {
			"type": "json",
			"root": "data"
		}
	}
});