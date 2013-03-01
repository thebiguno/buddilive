Ext.define("BuddiLive.store.account.TreeStore", {
	"extend": "Ext.data.TreeStore",
	"requires": [
		"BuddiLive.model.account.TreeModel"
	],
	"model": "BuddiLive.model.account.TreeModel",
	"proxy": {
		"type": "ajax",
		"url": "gui/accounts.json",
		"reader": {
			"type": "json",
			"root": "children"
		}
	},
	"autoload": true
});