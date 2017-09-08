Ext.define('BuddiLive.view.report.AccountBalancesOverTime', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.reportaccountbalancesovertime",
	
	"requires": [],
	
	"closable": true,
	"layout": "fit",
	"initComponent": function(){
		this.dockedItems = BuddiLive.app.viewport.getDockedItems();
		
		var fields = ["date"];
		var series = [];
		BuddiLive.app.viewport.down("accounttree").getStore().getRootNode().cascadeBy(function(node){
			if (node.data.nodeType == "account"){
				fields.push("a" + node.data.id);
				series.push({
					"type": "line",
					"axis": "left",
					"showMarkers": false,
					"style": {
						"stroke-width": 2
					},
					"title": node.data.name,
					"xField": "date",
					"yField": "a" + node.data.id
				});
				return true;
			}
		});
		
		this.title = "${translation("REPORT_ACCOUNT_BALANCES_OVER_TIME")?json_string} - " + this.initialConfig.options.dateRange;
		this.items = [
			{
				"xtype": "chart",
				"store": Ext.create("Ext.data.Store", {
					"autoLoad": true,
					"fields": fields,
					"proxy": {
						"type": "ajax",
						"url": "data/report/balancesovertime.json?" + this.initialConfig.options.query,
						"reader": {
							"type": "json",
							"rootProperty": "data"
						}
					}
				}),
				"legend": {
					"docked": "right"
				},
				axes: [
					{
						"type": "numeric",
						"position": "left",
						"fields": fields,
						"title": "${translation("ACCOUNT_BALANCE")?json_string}",
						"grid": true
					},
					{
						"type": "category",
						"position": "bottom",
						"label": {
							"rotate": {
								"degrees": -90
							}
						},
						"fields": ["date"],
						"title": "${translation("DATE")?json_string}"
					}
				],
				"series": series
			}
		]
	
		this.callParent(arguments);
	}
});