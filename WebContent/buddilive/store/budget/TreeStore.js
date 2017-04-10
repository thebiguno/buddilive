Ext.define("BuddiLive.store.budget.TreeStore", {
	"extend": "Ext.data.TreeStore",
	"requires": [
		"BuddiLive.model.budget.TreeModel"
	],
	"model": "BuddiLive.model.budget.TreeModel",
	"proxy": {
		"type": "ajax",
		"url": "data/categories.json?periodType=MONTH",
		"reader": {
			"type": "json",
			"rootProperty": "children",
			"transform": function(data){
				budgetTree = Ext.ComponentQuery.query('viewport')[0].down("budgettree");
				budgetTree.down("textfield[itemId='currentPeriod']").setValue(data.period);
				var columns = budgetTree.getView().headerCt.items.items;
				columns[1].setText(columns[1].initialConfig.text + " (" + data.previousPeriod + ")");
				columns[2].setText(columns[2].initialConfig.text + " (" + data.period + ")");
				budgetTree.currentDate = data.date;	//ISO Date string, will be used as current reference when passing nextPeriod / previousPeriod
				return data;	//We don't actually transform here, we just need a hook to get access to the raw data.  Return data unmolested.
			}
		}
	},
	"autoload": true
});