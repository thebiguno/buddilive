Ext.define("BuddiLive.store.Transactions", {
	"extend": "Ext.data.Store",
	"model": "BuddiLive.model.Transactions",
	"proxy": {
		"type": "ajax",
		"url": "gui/transactions.json",
		"reader": {
			"type": "json",
			"root": "children"
		}
	},
	"autoload": true
});