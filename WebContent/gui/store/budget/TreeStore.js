Ext.define("BuddiLive.store.budget.TreeStore", {
	"extend": "Ext.data.TreeStore",
	"requires": [
		"BuddiLive.model.budget.TreeModel"
	],
	"model": "BuddiLive.model.budget.TreeModel",
	"proxy": {
		"type": "ajax",
		"url": "gui/categories.json",
		"reader": {
			"type": "json",
			"root": "children"
		}
	},
	"autoload": true
});