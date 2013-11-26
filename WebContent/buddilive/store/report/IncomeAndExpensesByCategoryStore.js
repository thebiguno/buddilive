Ext.define("BuddiLive.store.report.IncomeAndExpensesByCategoryStore", {
	"extend": "Ext.data.Store",
	"requires": [
		"BuddiLive.model.report.SummaryModel"
	],
	"model": "BuddiLive.model.report.SummaryModel",
	"autoLoad": true,
	"constructor": function(config){
		this.proxy = {
			"type": "ajax",
			"url": "data/report/incomeandexpensesbycategory.json?" + config.query,
			"reader": {
				"type": "json",
				"root": "data"
			}
		}
		
		this.callParent(arguments);
	}
});