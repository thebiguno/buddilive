Ext.define("BuddiLive.store.transaction.split.FromStore", {
	"extend": "Ext.data.Store",
	"requires": [
		"BuddiLive.model.shared.Combobox"
	],
	"model": "BuddiLive.model.shared.Combobox",
	"proxy": {
		"type": "ajax",
		"url": "gui/sources/from.json",
		"reader": {
			"type": "json",
			"root": "data"
		}
	},
	"autoload": true
});