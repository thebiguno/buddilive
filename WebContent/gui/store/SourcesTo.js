Ext.define("BuddiLive.store.SourcesTo", {
	"extend": "Ext.data.Store",
	"model": "BuddiLive.model.Combobox",
	"proxy": {
		"type": "ajax",
		"url": "gui/sources/to.json",
		"reader": {
			"type": "json",
			"root": "data"
		}
	},
	"autoload": true
});