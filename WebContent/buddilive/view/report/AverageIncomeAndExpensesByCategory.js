Ext.define('BuddiLive.view.report.AverageIncomeAndExpensesByCategory', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.reportaverageincomeandexpensesbycategory",
	
	"requires": [],
	
	"closable": true,
	"layout": "fit",
	"initComponent": function(){
		this.dockedItems = BuddiLive.app.viewport.getDockedItems();
		
		this.title = "${translation("REPORT_TABLE_AVERAGE_INCOME_AND_EXPENSES_BY_CATEGORY")?json_string} - " + this.initialConfig.options.dateRange;
		var styledRenderer = function(value, metaData, record){
			metaData.style = record.data[metaData.column.dataIndex + "Style"];
			return value;
		};

		this.items = [
			{
				"xtype": "grid",
				"store": Ext.create("Ext.data.Store", {
					"autoLoad": true,
					"fields": ["source", "average"],
					"proxy": {
						"type": "ajax",
						"url": "data/report/averageincomeandexpensesbycategory.json?" + this.initialConfig.options.query,
						"reader": {
							"type": "json",
							"rootProperty": "data"
						}
					}
				}),
				"columns": [
					{
						"text": "${translation("BUDGET_CATEGORY_NAME")?json_string}",
						"dataIndex": "source",
						"hideable": false,
						"sortable": false,
						"flex": 3,
						"renderer": styledRenderer
					},
					{
						"text": "${translation("AVERAGE")?json_string}",
						"dataIndex": "average",
						"hideable": false,
						"sortable": false,
						"flex": 2,
						"renderer": styledRenderer
					},
					{
						"text": "${translation("BUDGET_CATEGORY_PERIOD_TYPE")?json_string}",
						"dataIndex": "period",
						"hideable": false,
						"sortable": false,
						"flex": 2,
						"renderer": styledRenderer
					}
				]
			}
		]
	
		this.callParent(arguments);
	}
});