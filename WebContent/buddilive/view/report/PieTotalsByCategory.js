Ext.define('BuddiLive.view.report.PieTotalsByCategory', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.reportpietotalsbycategory",
	
	"requires": [],
	
	"closable": true,
	"layout": "fit",
	"initComponent": function(){
		this.dockedItems = BuddiLive.app.viewport.getDockedItems();
		
		this.title = "${translation("REPORT_PIE_INCOME_BY_CATEGORY")?json_string} - " + this.initialConfig.options.dateRange;
		this.items = [
			{
				"xtype": "polar",
				"store": Ext.create("Ext.data.Store", {
					"autoLoad": true,
					"fields": ["label", "formattedAmount", "amount", "percent"],
					"proxy": {
						"type": "ajax",
						"url": "data/report/pietotalsbycategory.json?type=" + this.initialConfig.type + "&" + this.initialConfig.options.query,
						"reader": {
							"type": "json",
							"rootProperty": "data"
						}
					}
				}),
				"innerPadding": 40,
				"interactions": ['rotate', 'itemhighlight'],
				"legend": {
					"docked": "right"
				},
				"series": [{
					"type": "pie",
					"angleField": "amount",
					"showInLegend": true,
					"highlight": true,
					"label": {
						"field": "label",
						"display": "rotate"
					}
				}]
			}
		]
	
		this.callParent(arguments);
	}
});