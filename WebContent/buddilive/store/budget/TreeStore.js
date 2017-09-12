Ext.define("BuddiLive.store.budget.TreeStore", {
	"extend": "Ext.data.TreeStore",
	"requires": [],
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
	],
	"autoLoad": true,
	"constructor": function(config){
		config.proxy = {
			"type": "ajax",
			"url": "data/categories.json?periodType=" + config.periodType,
			"reader": {
				"type": "json",
				"rootProperty": "children",
				"transform": function(data){
					debugger;
					budgetTree = Ext.ComponentQuery.query('viewport')[0].down("budgettree[itemId='" + config.periodType + "']");
					budgetTree.down("textfield[itemId='currentPeriod']").setValue(data.period);
					var columns = budgetTree.getView().headerCt.items.items;
					columns[1].setText(columns[1].initialConfig.text + " (" + data.previousPeriod + ")");
					columns[2].setText(columns[2].initialConfig.text + " (" + data.period + ")");
					budgetTree.currentDate = data.date;	//ISO Date string, will be used as current reference when passing nextPeriod / previousPeriod
					return data;	//We don't actually transform here, we just need a hook to get access to the raw data.  Return data unmolested.
				}
			}
		};
		this.callParent(arguments);
	}
});