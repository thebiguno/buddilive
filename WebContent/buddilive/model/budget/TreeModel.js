Ext.define('BuddiLive.model.budget.TreeModel', {
	"extend": "Ext.data.TreeModel",
	"fields": [
		"id",
		"name",
		"nameStyle",
		"date",
		"type",
		"deleted",
		"previous",
		"previousStyle",
		"current",
		"currentStyle",
		"actual",
		"actualStyle",
		"difference",
		"differenceStyle"
	]
});