Ext.define('BuddiLive.model.budget.TreeModel', {
	"extend": "Ext.data.Model",
	"fields": [
		"id",
		"name",
		"type",
		"deleted",
		"previous",
		"current",
		"actual",
		"difference"
	]
});