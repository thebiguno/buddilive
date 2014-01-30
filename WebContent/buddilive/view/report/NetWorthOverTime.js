Ext.define('BuddiLive.view.report.NetWorthOverTime', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.reportnetworthovertime",
	
	"requires": [
		"BuddiLive.store.report.NetWorthOverTimeStore"
	],
	
	"closable": true,
	"layout": "fit",
	"initComponent": function(){
		this.dockedItems = BuddiLive.app.viewport.getDockedItems();
		
		this.title = "${translation("REPORT_NET_WORTH_OVER_TIME")?json_string} - " + this.initialConfig.options.dateRange;
		this.items = [
			{
				"xtype": "chart",
				"store": Ext.create("BuddiLive.store.report.NetWorthOverTimeStore", {"query": this.initialConfig.options.query}),
				"legend": {
					"position": "right"
				},
				axes: [
					{
						"type": "Numeric",
						"position": "left",
						"fields": ["netWorth"],
						"title": "${translation("NET_WORTH")?json_string}",
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
						"title": "${translation("DATE")?json_string}"
					}
				],
				"series": [
					{
						"type": "line",
						"axis": "left",
						"showMarkers": false,
						"style": {
							"stroke": "#bf3030",
							"stroke-width": 2
						},
						"title": "${translation("NET_WORTH")?json_string}",
						"xField": "date",
						"yField": "netWorth"
					}
				]
			}
		]
	
		this.callParent(arguments);
	}
});