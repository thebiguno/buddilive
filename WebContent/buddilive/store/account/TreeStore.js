Ext.define("BuddiLive.store.account.TreeStore", {
	"extend": "Ext.data.TreeStore",
	"requires": [
		"BuddiLive.model.account.TreeModel"
	],
	"model": "BuddiLive.model.account.TreeModel",
	"proxy": {
		"type": "ajax",
		"url": "buddilive/accounts.json",
		"reader": {
			"type": "json",
			"root": "children"
		}
	}
});