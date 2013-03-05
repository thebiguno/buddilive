Ext.define("BuddiLive.store.transaction.DescriptionStore", {
	"extend": "Ext.data.Store",
	"requires": [
		"BuddiLive.model.shared.Combobox"
	],
	"model": "BuddiLive.model.shared.Combobox",
	"proxy": {
		"type": "ajax",
		"url": "gui/transactions/descriptions.json",
		"reader": {
			"type": "json",
			"root": "children"
		}
	},
	"autoload": true
});