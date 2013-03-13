Ext.define("BuddiLive.store.transaction.split.FromComboboxStore", {
	"extend": "Ext.data.Store",
	"model": "BuddiLive.model.transaction.split.SourceComboboxModel",
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