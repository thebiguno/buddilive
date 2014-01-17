Ext.define('BuddiLive.model.budget.TreeModel', {
	"extend": "Ext.data.TreeModel",
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