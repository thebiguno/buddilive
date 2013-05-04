Ext.define('BuddiLive.view.report.PieTotalsByCategory', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.reportpietotalsbycategory",
	
	"requires": [
		"BuddiLive.store.report.IncomeAndExpensesByCategoryStore"
	],
	
	"initComponent": function(){
		this.title = "${translation("REPORT_PIE_INCOME_BY_CATEGORY")?json_string}",
		this.closable = true;
		this.dockedItems = BuddiLive.app.viewport.getDockedItems();
		this.layout = "fit";
		
		this.items = [
			{
				"xtype": "grid",
				"store": Ext.create("BuddiLive.store.report.IncomeAndExpensesByCategoryStore", {"query": this.initialConfig.query}),
				"columns": [
					{
						"text": "${translation("NAME")?json_string}",
						"dataIndex": "category",
						"hideable": false,
						"sortable": false,
						"flex": 2
					},
					{
						"text": "${translation("ACTUAL")?json_string}",
						"dataIndex": "actual",
						"hideable": false,
						"sortable": false,
						"flex": 1
					},
					{
						"text": "${translation("BUDGETED")?json_string}",
						"dataIndex": "budgeted",
						"hideable": false,
						"sortable": false,
						"flex": 1
					},
					{
						"text": "${translation("DIFFERENCE")?json_string}",
						"dataIndex": "difference",
						"hideable": false,
						"sortable": false,
						"flex": 1
					}
				]
			}
		]
	
		this.callParent(arguments);
	}
});