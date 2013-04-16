Ext.define("BuddiLive.store.transaction.split.ToComboboxStore", {
	"extend": "Ext.data.Store",
	"model": "BuddiLive.model.shared.Combobox",
	"remoteFilter": false,
	"proxy": {
		"type": "ajax",
		"autoAbort": true, 
		"url": "buddilive/sources/to.json",
		"reader": {
			"type": "json",
			"root": "data"
		}
	}
});