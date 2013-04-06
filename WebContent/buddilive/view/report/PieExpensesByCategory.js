Ext.define('BuddiLive.view.report.PieExpensesByCategory', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.reportpieexpensesbycategory",
	
	"requires": [
		"BuddiLive.store.report.PieExpensesByCategoryStore"
	],
	
	"initComponent": function(){
		this.title = "${translation("REPORT_PIE_EXPENSES_BY_CATEGORY")?json_string}",
		this.closable = true;
		this.dockedItems = BuddiLive.app.viewport.getDockedItems();
		this.layout = "fit";
		
		this.items = [
			{
				"xtype": "chart",
				"store": Ext.create("BuddiLive.store.report.PieExpensesByCategoryStore"),
				"series": [{
					"type": "pie",
					"angleField": "amount",
					"showInLegend": true,
					"tips": {
						"trackMouse": true,
						"width": 500,
						"height": 28,
						"renderer": function(storeItem, item) {
							this.setTitle(storeItem.get("label") + ": " + storeItem.get("amount"));
						}
					},
					"highlight": {
						"segment": {
							"margin": 20
						}
					},
					"label": {
						"field": "label",
						"display": "rotate",
						"contrast": true
					}
				}]
			}
		]
	
		this.callParent(arguments);
	}
});