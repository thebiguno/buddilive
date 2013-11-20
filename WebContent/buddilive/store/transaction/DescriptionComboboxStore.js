Ext.define("BuddiLive.store.transaction.DescriptionComboboxStore", {
	"extend": "Ext.data.Store",
	"model": "BuddiLive.model.shared.Combobox",
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
			"root": "data"
		}
	}
});