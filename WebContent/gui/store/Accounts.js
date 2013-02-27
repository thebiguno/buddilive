Ext.define("BuddiLive.store.Accounts", {
	"extend": "Ext.data.TreeStore",
	"model": "BuddiLive.model.Accounts",
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