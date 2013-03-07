Ext.define('BuddiLive.model.budget.TreeModel', {
	"extend": "Ext.data.Model",
	"fields": [
		"id", 
		"name", 
		"type", 
		"accountType",
		"startBalance",
		"deleted", 
		"previousId",
		"previousAmount",
		"previousDate",
		"currentId",
		"currentAmount",
		"currentDate",
		"nodeType"
	]
});