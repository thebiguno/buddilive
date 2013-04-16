Ext.define("BuddiLive.store.transaction.split.FromComboboxStore", {
	"extend": "Ext.data.Store",
	"model": "BuddiLive.model.shared.Combobox",
	"remoteFilter": false,
	"proxy": {
		"type": "ajax",
		"autoAbort": true, 
		"url": "buddilive/sources/from.json",
		"reader": {
			"type": "json",
			"root": "data"
		}
	}
});