Ext.define("BuddiLive.store.SourcesFrom", {
	"extend": "Ext.data.Store",
	"model": "BuddiLive.model.Combobox",
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