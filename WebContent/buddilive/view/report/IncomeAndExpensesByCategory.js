Ext.define('BuddiLive.view.report.IncomeAndExpensesByCategory', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.reportincomeandexpensesbycategory",
	
	"requires": [
		"BuddiLive.store.report.IncomeAndExpensesByCategoryStore"
	],
	
	"title": "${translation("REPORT_TABLE_INCOME_AND_EXPENSES_BY_CATEGORY")?json_string}",
	"closable": true,
	"layout": "fit",
	"initComponent": function(){
		this.dockedItems = BuddiLive.app.viewport.getDockedItems();
		
		var styledRenderer = function(value, metaData, record){
			metaData.style = record.raw[metaData.column.dataIndex + "Style"];
			return value;
		};

		this.items = [
			{
				"xtype": "grid",
				"store": Ext.create("BuddiLive.store.report.IncomeAndExpensesByCategoryStore", {"query": this.initialConfig.query, "type": this.initialConfig.type}),
				"plugins": [
					{
						"ptype": "rowexpander",
						"rowBodyTpl": [
							"<table class='x-grid-table' style='width: 100%;'><tr>",
							"<td class='x-grid-cell x-grid-td' style='width: 10%; font-weight: bold;'>Date</td>",
							"<td class='x-grid-cell x-grid-td' style='width: 40%; font-weight: bold;'>Description</td>",
							"<td class='x-grid-cell x-grid-td' style='width: 30%; font-weight: bold;'>From / To</td>",
							"<td class='x-grid-cell x-grid-td' style='width: 20%; font-weight: bold;'>Amount</td>",
							"</tr>",
							"<tpl for='transactions'><tr>",
							"<td class='x-grid-cell x-grid-td'>{date}</td>",
							"<td class='x-grid-cell x-grid-td'>{description}</td>",
							"<td class='x-grid-cell x-grid-td'>{from} &rarr; {to}</td>",
							"<td class='x-grid-cell x-grid-td'>{amount}</td>",
							"</tr></tpl>",
							"</table>"
						]
					}
				],
				"columns": [
					{
						"text": "${translation("BUDGET_CATEGORY_NAME")?json_string}",
						"dataIndex": "source",
						"hideable": false,
						"sortable": false,
						"flex": 25
					},
					{
						"text": "${translation("ACTUAL")?json_string}",
						"dataIndex": "actual",
						"hideable": false,
						"sortable": false,
						"flex": 25,
						"renderer": styledRenderer
					},
					{
						"text": "${translation("BUDGETED")?json_string}",
						"dataIndex": "budgeted",
						"hideable": false,
						"sortable": false,
						"flex": 25,
						"renderer": styledRenderer
					},
					{
						"text": "${translation("DIFFERENCE")?json_string}",
						"dataIndex": "difference",
						"hideable": false,
						"sortable": false,
						"flex": 25,
						"renderer": styledRenderer
					}
				]
			}
		]
	
		this.callParent(arguments);
	}
});