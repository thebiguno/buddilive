Ext.define('BuddiLive.model.budget.TreeModel', {
	"extend": "Ext.data.Model",
	"fields": [
		"id", 
		"name", 
		"type", 
		"deleted", 
		"previousAmount",
		"previousDate",
		"currentAmount",
		"currentDate",
		"actual",
		"difference"
	]
});