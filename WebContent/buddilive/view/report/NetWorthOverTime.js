Ext.define('BuddiLive.view.report.NetWorthOverTime', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.reportnetworthovertime",
	
	"requires": [],
	
	"closable": true,
	"layout": "fit",
	"initComponent": function(){
		this.dockedItems = BuddiLive.app.viewport.getDockedItems();
		
		this.title = "${translation("REPORT_NET_WORTH_OVER_TIME")?json_string} - " + this.initialConfig.options.dateRange;
		this.items = [
			{
				"xtype": "chart",
				"store": Ext.create("Ext.data.Store", {
					"autoLoad": true,
					"proxy": {
						"type": "ajax",
						"url": "data/report/balancesovertime.json?netWorthOnly=true&" + this.initialConfig.options.query,
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
						"fields": ["netWorth"],
						"title": "${translation("NET_WORTH")?json_string}",
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
				"series": [
					{
						"type": "line",
						"axis": "left",
						"showMarkers": false,
						"style": {
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