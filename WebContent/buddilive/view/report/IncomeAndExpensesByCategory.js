Ext.define('BuddiLive.view.report.PieTotalsByCategory', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.reportpietotalsbycategory",
	
	"requires": [
		"BuddiLive.store.report.IncomeAndExpensesByCategoryStore"
	],
	
	"title": "${translation("REPORT_PIE_INCOME_BY_CATEGORY")?json_string}",
	"closable": true,
	"layout": "fit",
	"initComponent": function(){
		this.dockedItems = BuddiLive.app.viewport.getDockedItems();
		
		this.items = [
			{
				"xtype": "chart",
				"store": Ext.create("BuddiLive.store.report.PieTotalsByCategoryStore", {"query": this.initialConfig.query, "type": this.initialConfig.type}),
				"legend": {
					"position": "bottom"
				},
				"series": [{
					"type": "bar",
					"showInLegend": true,
					"label": {
						"field": "label",
						"display": "rotate",
						"contrast": true,
						"renderer": function(value, storeItem, item) {
							return value + ": " + item.get("formattedAmount");
						}
					}
				}]
			}
		]
	
		this.callParent(arguments);
	}
});