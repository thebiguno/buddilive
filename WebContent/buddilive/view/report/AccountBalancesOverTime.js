Ext.define('BuddiLive.view.report.AccountBalancesOverTime', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.reportaccountbalancesovertime",
	
	"requires": [
		"BuddiLive.store.report.AccountBalancesOverTimeStore"
	],
	
	"closable": true,
	"layout": "fit",
	"initComponent": function(){
		this.dockedItems = BuddiLive.app.viewport.getDockedItems();
		
		var colors = ["#bf3030", "#bf8630", "#a3bf30", "#4dbf30", "#30bf69", "#30bfbf", "#3069bf", "#4c30bf", "#a330bf", "#bf3086"];
		var colorCounter = 0;
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
						"stroke": colors[colorCounter],
						"stroke-width": 2
					},
					"title": node.data.name,
					"xField": "date",
					"yField": "a" + node.data.id
				});
				colorCounter++;
				if (colorCounter >= colors.length) colorCounter = 0;
				return true;
			}
		});
		
		this.title = "${translation("REPORT_ACCOUNT_BALANCES_OVER_TIME")?json_string} - " + this.initialConfig.options.dateRange;
		this.items = [
			{
				"xtype": "chart",
				"store": Ext.create("BuddiLive.store.report.AccountBalancesOverTimeStore", {"query": this.initialConfig.options.query, "fields": fields}),
				"legend": {
					"position": "right"
				},
				axes: [
					{
						"type": "Numeric",
						"position": "left",
						"fields": fields,
						"title": "TODO Balances",
						"grid": true
					},
					{
						"type": "Category",
						"position": "bottom",
						"label": {
							"rotate": {
								"degrees": -90
							}
						},
						"fields": ["date"],
						"title": "TODO Date"
					}
				],
				"series": series
			}
		]
	
		this.callParent(arguments);
	}
});