Ext.define("BuddiLive.store.transaction.DescriptionComboboxStore", {
	"extend": "Ext.data.Store",
	"model": "BuddiLive.model.shared.Combobox",
	"remoteFilter": false,
	"proxy": {
		"type": "ajax",
		"autoAbort": true, 
		"url": "buddilive/transactions/descriptions.json",
		"reader": {
			"type": "json",
			"root": "data"
		}
	}
});