Ext.define('BuddiLive.view.report.InflowAndOutflowByAccount', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.reportinflowandoutflowbyaccount",
	
	"requires": [
	],
	
	"closable": true,
	"layout": "fit",
	"initComponent": function(){
		var me = this;
		this.dockedItems = BuddiLive.app.viewport.getDockedItems("report");
		
		this.title = "${translation("REPORT_TABLE_INFLOW_AND_OUTFLOW_BY_ACCOUNT")?json_string} - " + this.initialConfig.options.dateRange;
		var styledRenderer = function(value, metaData, record){
			metaData.style = record.data[metaData.column.dataIndex + "Style"];
			return value;
		};

		this.items = [
			{
				"xtype": "grid",
				"store": Ext.create("Ext.data.Store", {
					"autoLoad": true,
					"fields": ["source", "inflow", "outflow", "difference", "transactions"],
					"proxy": {
						"type": "ajax",
						"url": "data/report/inflowandoutflowbyaccount.json?" + this.initialConfig.options.query,
						"reader": {
							"type": "json",
							"rootProperty": "data"
						}
					}
				}),
				"plugins": [
					{
						"ptype": "rowexpander",
						"expandOnEnter": false,
						"rowBodyTpl": [
							"<table class='x-grid-table' style='width: 100%;'>",
							"<tpl if='transactions.length &gt; 0'>",
								"<tr>",
									"<td class='x-grid-cell x-grid-td' style='width: 10%; font-weight: bold;'>${translation("DATE")?json_string}</td>",
									"<td class='x-grid-cell x-grid-td' style='width: 20%; font-weight: bold;'>${translation("DESCRIPTION")?json_string}</td>",
									"<td class='x-grid-cell x-grid-td' style='width: 20%; font-weight: bold;'>${translation("FROM")?json_string} &rarr; ${translation("TO")?json_string}</td>",
									"<td class='x-grid-cell x-grid-td' style='width: 20%; font-weight: bold;'>${translation("AMOUNT")?json_string}</td>",
								"</tr>",
								"<tpl for='transactions'>",
									"<tr>",
										"<td class='x-grid-cell x-grid-td' style='{dateStyle}'>{date}</td>",
										"<td class='x-grid-cell x-grid-td' style='{descriptionStyle}'>{description}</td>",
										"<td class='x-grid-cell x-grid-td' style='{fromToStyle}'>{from} &rarr; {to}</td>",
										"<td class='x-grid-cell x-grid-td' style='{amountStyle}'>{amount}</td>",
									"</tr>",
								"</tpl>",
							"<tpl else>",
								"<tr>",
									"<td class='x-grid-cell x-grid-td' style='width: 100%; font-weight: bold;'>${translation("NO_TRANSACTIONS_IN_SELECTED_RANGE")?json_string}</td>",
								"</tr>",
							"</tpl>",
							"</table>"
						],
						"toggleRow" : function(rowIdx, record) {
							if (record.get("source") == "${translation("TOTAL")?json_string}") return false;
							Ext.grid.plugin.RowExpander.prototype.toggleRow.apply(this, arguments);
						}
					}
				],
				"columns": [
					{
						"text": "${translation("ACCOUNT_NAME")?json_string}",
						"dataIndex": "source",
						"hideable": false,
						"sortable": false,
						"flex": 3,
						"renderer": styledRenderer
					},
					{
						"text": "${translation("INFLOW")?json_string}",
						"dataIndex": "inflow",
						"hideable": false,
						"sortable": false,
						"flex": 2,
						"renderer": styledRenderer
					},
					{
						"text": "${translation("OUTFLOW")?json_string}",
						"dataIndex": "outflow",
						"hideable": false,
						"sortable": false,
						"flex": 2,
						"renderer": styledRenderer
					},
					{
						"text": "${translation("DIFFERENCE")?json_string}",
						"dataIndex": "difference",
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