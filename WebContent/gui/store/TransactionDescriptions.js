Ext.define("BuddiLive.store.TransactionDescriptions", {
	"extend": "Ext.data.Store",
	"model": "BuddiLive.model.TransactionDescriptions",
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