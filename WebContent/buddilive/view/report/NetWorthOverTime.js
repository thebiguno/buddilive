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
					"position": "bottom"
				},
				axes: [
					{
						type: 'Numeric',
						position: 'left',
						fields: ['amount'],
						title: 'Net Worth',
						grid: true,
						minimum: 0
					},
					{
						type: 'Category',
						position: 'bottom',
						fields: ['date'],
						title: 'Date'
					}
				],
				"series": [{
					"type": "line",
					axis: 'left',
					xField: 'date',
					yField: 'amount',
					markerConfig: {
						type: 'cross',
						size: 4,
						radius: 4,
						'stroke-width': 0
					}
				}]
			}
		]
	
		this.callParent(arguments);
	}
});